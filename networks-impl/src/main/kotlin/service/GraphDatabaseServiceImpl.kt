package service

import configuration.DbmsInstancesConfiguration
import factory.Factory
import factory.LeftSplitFactory
import factory.RightSplitFactory
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

class GraphDatabaseServiceImpl(
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val detachDeleteAllQuery: DetachDeleteAllQuery,
    private val insertLeftSplitQueryFactory: (LeftSplit, Boolean) -> InsertLeftSplitQuery,
    private val insertRightSplitQueryFactory: (RightSplit, Boolean) -> InsertRightSplitQuery,
    private val leftSplitFactory: LeftSplitFactory,
    private val matchAllQuery: MatchAllQuery,
    private val rightSplitFactory: RightSplitFactory,
    private val userFactory: Factory<User>
) : GraphDatabaseService {

    private val logger = Logger.getLogger(GraphDatabaseService::class.simpleName)
    private lateinit var driver: Driver

    /**
     * Begins a session and executes the given block of code.
     *
     * @param block The block of code to execute.
     *
     * @return The result of the block of code.
     */
    private fun <R> beginSession(block: Session.() -> R): R {
        return driver
            .session(SessionConfig.forDatabase(dbmsInstancesConfiguration.compositeName))
            .use { session ->
                block(session)
            }
    }

    override suspend fun deleteGraph() {
        withContext(Dispatchers.IO) {
            beginSession {
                executeWrite { context ->
                    detachDeleteAllQuery
                        .cypherize()
                        .forEach { query ->
                            context
                                .run(query)
                                .consume()
                        }
                }
            }
        }
    }
    
    // TODO: Create graph labels indexes to speed up the queries (which we do not have).
    override suspend fun generateNodes(amount: Int) {
        withContext(Dispatchers.IO) {
            val users = userFactory.create(amount)

            beginSession {
                executeWrite { context ->
                    generateNodesInLeftSplit(
                        context = context,
                        users = users
                    )

                    generateNodesInRightSplit(
                        context = context,
                        users = users
                    )
                }
            }
        }
    }

    // TODO: Create graph labels indexes to speed up the queries (which we do not have).
    override suspend fun generateNodesInLeftSplit(amount: Int) {
        withContext(Dispatchers.IO) {
            val users = userFactory.create(amount)

            beginSession {
                executeWrite { context ->
                    generateNodesInLeftSplit(
                        context = context,
                        users = users
                    )
                }
            }
        }
    }

    private fun generateNodesInLeftSplit(
        context: TransactionContext,
        users: List<User>
    ) {
        val (leftSplit1, leftSplit2) = splitHorizontally(leftSplitFactory.create(users))

        for (leftSplit in listOf(
            leftSplit1,
            leftSplit2
        )) {
            insertLeftSplitQueryFactory(
                leftSplit,
                leftSplit == leftSplit1
            )
                .cypherize()
                .forEach { query ->
                    context
                        .run(query)
                        .consume()
                }
        }
    }

    // TODO: Create graph labels indexes to speed up the queries (which we do not have).
    override suspend fun generateNodesInRightSplit(amount: Int) {
        withContext(Dispatchers.IO) {
            val users = userFactory.create(amount)

            beginSession {
                executeWrite { context ->
                    generateNodesInRightSplit(
                        context = context,
                        users = users
                    )
                }
            }
        }
    }

    private fun generateNodesInRightSplit(
        context: TransactionContext,
        users: List<User>
    ) {
        val (rightSplit1, rightSplit2) = splitHorizontally(rightSplitFactory.create(users))

        for (rightSplit in listOf(
            rightSplit1,
            rightSplit2
        )) {
            insertRightSplitQueryFactory(
                rightSplit,
                rightSplit == rightSplit1
            )
                .cypherize()
                .forEach { query ->
                    context
                        .run(query)
                        .consume()
                }
        }
    }

    override suspend fun getGraph(): List<List<Map<String, Any>>> {
        return beginSession {
            executeRead { context ->
                matchAllQuery
                    .cypherize()
                    .flatMap { query ->
                        context
                            .run(query)
                            .list { record ->
                                record
                                    .values()
                                    .map { value ->
                                        value.asMap()
                                    }
                            }
                    }
            }
        }
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