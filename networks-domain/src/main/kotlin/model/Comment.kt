package model

import java.time.Instant
import java.util.*

/**
 * Represents a comment made by a user on a post.
 *
 * @property commentedAt The time the comment was made.
 * @property content The content of the comment.
 * @property id The unique identifier of the comment.
 * @property post The post the comment was made on.
 * @property user The user who made the comment.
 */
data class Comment(
    val commentedAt: Instant = Instant.now(),
    val content: String,
    val id: String = UUID
        .randomUUID()
        .toString(),
    val post: Post = Post(),
    val user: UserSplitLeft = UserSplitLeft()
)