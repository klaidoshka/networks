package model

import java.time.Instant
import java.util.*

/**
 * Represents a post made by a user.
 *
 * @property comments The comments made on the post.
 * @property content The content of the post.
 * @property id The unique identifier of the post.
 * @property likes The likes made on the post.
 * @property postedAt The time the post was made.
 * @property user The user who made the post.
 */
data class Post(
    val comments: List<Comment> = emptyList(),
    val content: String = "",
    val id: String = UUID
        .randomUUID()
        .toString(),
    val likes: List<Like> = emptyList(),
    val postedAt: Instant = Instant.now(),
    val user: UserSplitLeft = UserSplitLeft()
)