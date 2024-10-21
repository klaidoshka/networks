package service

import model.LeftSplit
import model.RightSplit

/**
 * Graph database service. It is responsible for managing the graph database.
 *
 * There is no actual singular CRUD operations, but rather a set of specific operations
 * for demonstration purposes.
 */
interface GraphDatabaseService {

    /**
     * Delete the whole graph
     */
    suspend fun deleteGraph()

    /**
     * Generate a user, a post, few comments and few likes on the post
     *
     * @param amount Amount of generations to make
     */
    suspend fun generateNodesInLeftSplit(amount: Int)

    /**
     * Generate a user, a group, a membership, a friendship and some messages between two users
     *
     * @param amount Amount of generations to make
     */
    suspend fun generateNodesInRightSplit(amount: Int)

    /**
     * Generate all type nodes and random relationships between them
     *
     * @param amount Amount of nodes to generate
     */
    suspend fun generateNodes(amount: Int)

    /**
     * Get the graph
     *
     * @return Nodes and relationships mapped into cytoscape library format
     */
    suspend fun getGraph(): Map<String, Any>

    /**
     * Split the graph data horizontally
     *
     * @param leftSplit Left split data in graph to split
     *
     * @return Pair of left splits, divided horizontally by some criteria
     */
    fun splitHorizontally(leftSplit: LeftSplit): Pair<LeftSplit, LeftSplit>

    /**
     * Split the graph data horizontally
     *
     * @param rightSplit Right split data in graph to split
     *
     * @return Pair of right splits, divided horizontally by some criteria
     */
    fun splitHorizontally(rightSplit: RightSplit): Pair<RightSplit, RightSplit>

    /**
     * Start database driver
     */
    suspend fun start()

    /**
     * Stop database driver and close all connections
     */
    suspend fun stop()
}