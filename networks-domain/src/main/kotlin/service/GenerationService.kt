package service

/**
 * Service for generating nodes and relationships between them.
 *
 * It can generate nodes in two different splits or generate all nodes at once.
 */
interface GenerationService {

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
}