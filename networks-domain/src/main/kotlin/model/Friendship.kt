package model

import java.time.Instant
import java.util.*

/**
 * Represents a friendship between two users.
 *
 * @property id The unique identifier of the friendship.
 * @property since The time the friendship was established.
 * @property user1 The first user in the friendship.
 * @property user2 The second user in the friendship.
 */
data class Friendship(
    val id: String = UUID
        .randomUUID()
        .toString(),
    val since: Instant = Instant.now(),
    val user1: UserSplitRight = UserSplitRight(),
    val user2: UserSplitRight = UserSplitRight()
)