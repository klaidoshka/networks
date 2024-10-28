package service

import configuration.DbmsInstancesConfiguration
import factory.Factory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.User
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import query.DeleteGraphQuery
import query.GetGraphQuery
import util.DriverUtil.runInParallel
import util.logger
import kotlin.time.Duration.Companion.seconds

// TODO: Create graph labels indexes to speed up the queries (which we do not have).

class DatabaseServiceImpl(
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val deleteGraphQueryFactory: (String) -> DeleteGraphQuery,
    private val getGraphQueryFactory: (String) -> GetGraphQuery,
    private val userFactory: Factory<User>
) : DatabaseService {

    private lateinit var _driver: Driver
    override val driver: Driver
        get() = _driver

    private val logger = logger()

    override suspend fun deleteGraph() {
        driver.runInParallel(
            dbmsInstancesConfiguration.compositeName,
            dbmsInstancesConfiguration.databaseNames.associateWith { database ->
                { transaction -> deleteGraphQueryFactory(database)(transaction) }
            }
        )
    }

    override suspend fun getGraph(): Map<String, Any> {
        val result = driver
            .runInParallel(
                dbmsInstancesConfiguration.compositeName,
                dbmsInstancesConfiguration.databaseNames.associateWith { database ->
                    { transaction -> getGraphQueryFactory(database)(transaction) }
                }
            ).values
            .flatten()

        val cytoscape = result
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
                    ),
                )
            }
            .filter { it["data"]!!["id"]!!.isNotBlank() }
            .distinct()

        val elements = result
            .flatMap { (node, relationship, relatedNode) ->
                val nodeMap = node.asMap() + ("__data" to mapOf(
                    "label" to node
                        .labels()
                        .joinToString(",")
                ))

                val relatedNodeMap = relatedNode.asMap() + ("__data" to mapOf(
                    "label" to relatedNode
                        .labels()
                        .joinToString(",")
                ))

                val relationshipMap = relationship.asMap() + ("__data" to mapOf(
                    "label" to relationship.type(),
                    "source" to node["id"].asString(),
                    "sourceType" to nodeMap["label"],
                    "target" to relatedNode["id"].asString(),
                    "targetType" to relatedNodeMap["label"]
                ))

                listOf(
                    nodeMap,
                    relatedNodeMap,
                    relationshipMap
                )
            }
            .distinct()
            .sortedBy {
                @Suppress("UNCHECKED_CAST")
                (it["__data"] as Map<String, Any>)["label"] as String
            }

        return mapOf(
            "cytoscape" to cytoscape,
            "elements" to elements
        )
    }

    override suspend fun start() {
        withContext(Dispatchers.IO) {
            try {
                _driver = GraphDatabase.driver(
                    dbmsInstancesConfiguration.credentials.uri,
                    AuthTokens.basic(
                        dbmsInstancesConfiguration.credentials.username,
                        dbmsInstancesConfiguration.credentials.password
                    )
                )

                _driver.verifyConnectivity()

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
                if (::_driver.isInitialized) {
                    _driver.close()

                    logger.info("Driver stopped")
                }
            } catch (e: Exception) {
                logger.severe("Driver stop failed: ${e.message}")
            }
        }
    }
}