package service

interface DatabaseService {

    /**
     * Create a random friendship between two users with relationships
     */
    suspend fun createRandomFriendship()

    /**
     * Create a random group with relationships
     */
    suspend fun createRandomGroup()

    /**
     * Create a random post with relationships
     */
    suspend fun createRandomPost()

    /**
     * Get the graph data from Neo4j database
     * 
     * @return List of nodes and relationships
     */
    suspend fun getGraph(): List<List<Map<String, Any>>>

    /**
     * Reseed Neo4j database.
     *
     * This will delete all data and reseed the database with initial data.
     */
    suspend fun reseed()

    /**
     * Start Neo4j driver
     */
    suspend fun start()

    /**
     * Stop Neo4j driver and close all connections
     */
    suspend fun stop()
}