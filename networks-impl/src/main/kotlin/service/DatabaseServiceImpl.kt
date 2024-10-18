package service

import factory.Factory
import factory.LeftSplitFactory
import factory.RightSplitFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.User
import org.neo4j.driver.*
import query.DetachDeleteAllQuery
import query.SeedLeftSplitQuery
import query.SeedRightSplitQuery
import java.net.URI
import java.util.logging.Logger
import kotlin.time.Duration.Companion.seconds

class DatabaseServiceImpl(
    private val leftSplitFactory: LeftSplitFactory,
    private val rightSplitFactory: RightSplitFactory,
    private val userFactory: Factory<User>
) : DatabaseService {

    private val logger = Logger.getLogger(DatabaseService::class.simpleName)
    private lateinit var driver: Driver

    override suspend fun createRandomFriendship() {
        TODO("Not yet implemented")
    }

    override suspend fun createRandomGroup() {
        TODO("Not yet implemented")
    }

    override suspend fun createRandomPost() {
        TODO("Not yet implemented")
    }

    private fun <R> beginSession(block: Session.() -> R): R {
        return driver
            .session(SessionConfig.forDatabase("initial"))
            .use { session ->
                block(session)
            }
    }

    override suspend fun getGraph(): List<List<Map<String, Any>>> {
        return beginSession {
            run(
                """
                    MATCH (n)
                    RETURN n
                    """.trimIndent()
            ).list { record ->
                record
                    .values()
                    .map { value ->
                        value.asMap()
                    }
            }
        }
    }

    override suspend fun reseed() {
        withContext(Dispatchers.IO) {
            val users = userFactory.create(10)
            val leftSplitQuery = SeedLeftSplitQuery(leftSplitFactory.create(users))
            // TODO: Make sure dates are within user's registrationDate-lastActiveAt range
            val rightSplitQuery = SeedRightSplitQuery(rightSplitFactory.create(users))

            beginSession {
                executeWrite { context ->
                    DetachDeleteAllQuery
                        .cypherize()
                        .forEach { query ->
                            context
                                .run(query)
                                .consume()
                        }

                    leftSplitQuery
                        .cypherize()
                        .forEach { query ->
                            context
                                .run(query)
                                .consume()
                        }

                    rightSplitQuery
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

    override suspend fun start() {
        withContext(Dispatchers.IO) {
            try {
                driver = GraphDatabase.driver(
                    URI("bolt://localhost:7687"),
                    AuthTokens.basic(
                        "neo4j",
                        "networks"
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
}