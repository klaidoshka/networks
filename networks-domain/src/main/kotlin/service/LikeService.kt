package service

import model.Like

/**
 * Like service interface for liked posts related operations.
 */
interface LikeService {

    /**
     * Creates a like for a post.
     *
     * @param userId The unique identifier of the user who liked the post.
     * @param postId The unique identifier of the post to like.
     *
     * @return The like created.
     */
    suspend fun create(
        userId: String,
        postId: String
    ): Like

    /**
     * Deletes a like by its unique identifier.
     *
     * @param id The unique identifier of the like to delete.
     */
    suspend fun delete(id: String)

    /**
     * Gets all likes.
     *
     * @return A list of all likes.
     */
    suspend fun getAll(): List<Like>

    /**
     * Gets a like by its unique identifier.
     *
     * @param id The unique identifier of the like to get.
     * @param postId The unique identifier of the post to get.
     */
    suspend fun update(
        id: String,
        postId: String
    )
}