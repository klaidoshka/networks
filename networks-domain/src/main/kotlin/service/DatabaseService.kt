package service

import org.neo4j.driver.Driver

/**
 * Graph database service. It is responsible for managing the graph database.
 *
 * There is no actual singular CRUD operations, but rather a set of specific operations
 * for demonstration purposes.
 */
interface DatabaseService {

    /**
     * Driver of the graph database. Used to connect to the database and execute queries.
     */
    val driver: Driver

    /**
     * Delete the whole graph
     */
    suspend fun deleteGraph()

    /**
     * Get the graph. Data contains cytoscape library's data under the key "cytoscape".
     *
     * @return Nodes and relationships
     */
    suspend fun getGraph(): Map<String, Any>

    /**
     * Start database driver
     */
    suspend fun start()

    /**
     * Stop database driver and close all connections
     */
    suspend fun stop()
}