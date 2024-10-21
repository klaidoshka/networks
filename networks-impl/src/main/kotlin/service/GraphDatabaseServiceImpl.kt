package service

import configuration.DbmsInstancesConfiguration
import factory.Factory
import factory.LeftSplitFactory
import factory.RightSplitFactory
import handler.ParallelTransactionsHandler
import handler.ParallelTransactionsHandler.TransactionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.LeftSplit
import model.RightSplit
import model.User
import org.neo4j.driver.*
import query.DetachDeleteAllQuery
import query.InsertLeftSplitQuery
import query.InsertRightSplitQuery
import query.MatchAllQuery
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.logging.Logger
import kotlin.time.Duration.Companion.seconds

// TODO: Create graph labels indexes to speed up the queries (which we do not have).

class GraphDatabaseServiceImpl(
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val detachDeleteAllQueryFactory: (String) -> DetachDeleteAllQuery,
    private val insertLeftSplitQueryFactory: (LeftSplit, toPrimary: Boolean) -> InsertLeftSplitQuery,
    private val insertRightSplitQueryFactory: (RightSplit, toPrimary: Boolean) -> InsertRightSplitQuery,
    private val leftSplitFactory: LeftSplitFactory,
    private val matchAllQueryFactory: (String) -> MatchAllQuery,
    private val rightSplitFactory: RightSplitFactory,
    private val userFactory: Factory<User>
) : GraphDatabaseService {

    private val logger = Logger.getLogger(GraphDatabaseService::class.simpleName)
    private lateinit var driver: Driver

    /**
     * Execute parallel operations on the database. If any of the transactions fail, all transactions
     * are rolled back.
     *
     * In case of **success**, the holding result is of type [Map] with the id of the transaction as the key
     * and the result of the transaction as the value.
     *
     * @param transactions The transactions to execute.
     *
     * @return [TransactionResult.Success] if all transactions were successful, [TransactionResult.Failure]
     * otherwise.
     */
    private suspend fun <K, R> runParallelSessions(
        transactions: Map<K, (Transaction) -> R>
    ): Map<K, R> {
        return ParallelTransactionsHandler(
            sessionFactory = { driver.session(SessionConfig.forDatabase(dbmsInstancesConfiguration.compositeName)) },
            transactions = transactions
        )
            .handle()
            .let {
                if (it is TransactionResult.Failure) {
                    throw RuntimeException("Failed to execute transactions: ${it.error.message}")
                } else {
                    @Suppress("UNCHECKED_CAST")
                    (it as TransactionResult.Success<K, R>).result
                }
            }
    }

    override suspend fun deleteGraph() {
        runParallelSessions(
            dbmsInstancesConfiguration.databaseNames.associateWith { database ->
                { transaction ->
                    detachDeleteAllQueryFactory(database)
                        .cypherize()
                        .forEach { query ->
                            transaction
                                .run(query)
                                .consume()
                        }
                }
            }
        )
    }

    override suspend fun generateNodes(amount: Int) {
        val users = userFactory.create(amount)
        val (leftSplit1, leftSplit2) = splitHorizontally(leftSplitFactory.create(users))
        val (rightSplit1, rightSplit2) = splitHorizontally(rightSplitFactory.create(users))

        runParallelSessions(
            listOf(
                leftSplit1,
                leftSplit2,
                rightSplit1,
                rightSplit2
            )
                .associateWith { split ->
                    { transaction ->
                        when (split) {
                            is LeftSplit -> generateNodesInLeftSplit(
                                transaction,
                                split,
                                leftSplit1
                            )

                            is RightSplit -> generateNodesInRightSplit(
                                transaction,
                                split,
                                rightSplit1
                            )
                        }
                    }
                }
        )
    }

    override suspend fun generateNodesInLeftSplit(amount: Int) {
        val users = userFactory.create(amount)
        val (leftSplit1, leftSplit2) = splitHorizontally(leftSplitFactory.create(users))

        runParallelSessions(
            listOf(
                leftSplit1,
                leftSplit2
            )
                .associateWith { split ->
                    { transaction ->
                        generateNodesInLeftSplit(
                            transaction,
                            split,
                            leftSplit1
                        )
                    }
                }
        )
    }

    private fun generateNodesInLeftSplit(
        transaction: Transaction,
        leftSplit: LeftSplit,
        leftSplitPrimary: LeftSplit
    ) {
        insertLeftSplitQueryFactory(
            leftSplit,
            leftSplit == leftSplitPrimary
        )
            .cypherize()
            .forEach { query ->
                transaction
                    .run(query)
                    .consume()
            }
    }

    override suspend fun generateNodesInRightSplit(amount: Int) {
        val users = userFactory.create(amount)
        val (rightSplit1, rightSplit2) = splitHorizontally(rightSplitFactory.create(users))

        runParallelSessions(
            listOf(
                rightSplit1,
                rightSplit2
            )
                .associateWith { split ->
                    { transaction ->
                        generateNodesInRightSplit(
                            transaction,
                            split,
                            rightSplit1
                        )
                    }
                }
        )
    }

    private fun generateNodesInRightSplit(
        transaction: Transaction,
        rightSplit: RightSplit,
        rightSplitPrimary: RightSplit
    ) {
        insertRightSplitQueryFactory(
            rightSplit,
            rightSplit == rightSplitPrimary
        )
            .cypherize()
            .forEach { query ->
                transaction
                    .run(query)
                    .consume()
            }
    }

    override suspend fun getGraph(): Map<String, Any> {
        val result = runParallelSessions(
            dbmsInstancesConfiguration.databaseNames.associateWith { database ->
                { transaction ->
                    matchAllQueryFactory(database)
                        .cypherize()
                        .flatMap { query ->
                            transaction
                                .run(query)
                                .list()
                                .map { record ->
                                    Triple(
                                        record[MatchAllQuery.NODE_KEY].asNode(),
                                        record[MatchAllQuery.RELATIONSHIP_KEY].asRelationship(),
                                        record[MatchAllQuery.RELATED_NODE_KEY].asNode()
                                    )
                                }
                        }
                }
            }
        )

        val elements = result.values
            .flatten()
            .flatMap { (node, relationship, relatedNode) ->
                listOf(
                    mapOf(
                        "data" to mapOf(
                            "id" to node.elementId(),
                            "label" to node
                                .labels()
                                .joinToString(",")
                        )
                    ),
                    mapOf(
                        "data" to mapOf(
                            "id" to relationship.elementId(),
                            "source" to relationship.startNodeElementId(),
                            "target" to relationship.endNodeElementId()
                        )
                    ),
                    mapOf(
                        "data" to mapOf(
                            "id" to relatedNode.elementId(),
                            "label" to relatedNode
                                .labels()
                                .joinToString(",")
                        )
                    )
                )
            }

        return mapOf("elements" to elements)
    }

    override fun splitHorizontally(leftSplit: LeftSplit): Pair<LeftSplit, LeftSplit> {
        val now = Instant.now()

        val (comments, comments2) = leftSplit.comments.partition { it.commentedAt.isWithin6Months(now) }
        val (likes, likes2) = leftSplit.likes.partition { it.likedAt.isWithin6Months(now) }
        val (posts, posts2) = leftSplit.posts.partition { it.postedAt.isWithin6Months(now) }
        val (users, users2) = leftSplit.users.partition { it.lastActiveAt.isWithin6Months(now) }

        return LeftSplit(
            comments = comments,
            likes = likes,
            posts = posts,
            users = users
        ) to LeftSplit(
            comments = comments2,
            likes = likes2,
            posts = posts2,
            users = users2
        )
    }

    override fun splitHorizontally(rightSplit: RightSplit): Pair<RightSplit, RightSplit> {
        val now = Instant.now()

        val (friendships, friendships2) = rightSplit.friendships.partition { it.since.isWithin6Months(now) }
        val (groups, groups2) = rightSplit.groups.partition { it.createdAt.isWithin6Months(now) }
        val (memberships, memberships2) = rightSplit.memberships.partition { it.since.isWithin6Months(now) }
        val (messages, messages2) = rightSplit.messages.partition { it.sentAt.isWithin6Months(now) }
        val (users, users2) = rightSplit.users.partition { it.lastActiveAt.isWithin6Months(now) }

        return RightSplit(
            friendships = friendships,
            groups = groups,
            memberships = memberships,
            messages = messages,
            users = users
        ) to RightSplit(
            friendships = friendships2,
            groups = groups2,
            memberships = memberships2,
            messages = messages2,
            users = users2
        )
    }

    override suspend fun start() {
        withContext(Dispatchers.IO) {
            try {
                driver = GraphDatabase.driver(
                    dbmsInstancesConfiguration.credentials.uri,
                    AuthTokens.basic(
                        dbmsInstancesConfiguration.credentials.username,
                        dbmsInstancesConfiguration.credentials.password
                    )
                )

                driver.verifyConnectivity()

                logger.info("Driver started")
            } catch (e: Exception) {
                logger.severe("Driver start failed: ${e.message}")

                launch {
                    delay(10.seconds)

                    start()
                }
            }
        }
    }

    override suspend fun stop() {
        withContext(Dispatchers.IO) {
            try {
                if (::driver.isInitialized) {
                    driver.close()

                    logger.info("Driver stopped")
                }
            } catch (e: Exception) {
                logger.severe("Driver stop failed: ${e.message}")
            }
        }
    }

    companion object {


        /**
         * Check if the instant is within 6 months.
         *
         * @param now The current instant.
         *
         * @return True if the instant is within 6 months, false otherwise.
         */
        fun Instant.isWithin6Months(now: Instant) = this > now.minus(
            6 * 30,
            ChronoUnit.DAYS
        )
    }
}