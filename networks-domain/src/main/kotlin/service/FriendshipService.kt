package service

import model.Friendship
import java.time.Instant

/**
 * Friendship service interface. Handles minimal operations on friendships.
 */
interface FriendshipService {

    /**
     * Create a friendship between two users.
     *
     * @param userId1 ID of the first user
     * @param userId2 ID of the second user
     * @param since Timestamp of the friendship
     *
     * @return Friendship object
     */
    suspend fun create(
        userId1: String,
        userId2: String,
        since: Instant
    ): Friendship

    /**
     * Delete a friendship by its ID.
     *
     * @param id ID of the friendship
     */
    suspend fun delete(id: String)

    /**
     * Retrieve all friendships.
     *
     * @return List of friendships
     */
    suspend fun getAll(): List<Map<String, Any>>

    /**
     * Update a friendship's date by its ID.
     *
     * @param id ID of the friendship
     * @param since Timestamp of the friendship
     */
    suspend fun updateDate(
        id: String,
        since: Instant
    )
}