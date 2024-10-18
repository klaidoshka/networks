package model

import java.time.Instant
import java.util.*

/**
 * Represents a like made by a user on a post.
 *
 * @property id The unique identifier of the like.
 * @property likedAt The time the like was made.
 * @property post The post that was liked.
 * @property user The user that made the like.
 */
data class Like(
    val id: String = UUID
        .randomUUID()
        .toString(),
    val likedAt: Instant = Instant.now(),
    val post: Post = Post(),
    val user: UserSplitLeft = UserSplitLeft()
)