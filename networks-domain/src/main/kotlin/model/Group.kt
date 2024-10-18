package model

import java.time.Instant
import java.util.*

/**
 * Represents a group of users.
 *
 * @property createdAt The time the group was created.
 * @property description The description of the group.
 * @property id The unique identifier of the group.
 * @property name The name of the group.
 * @property user The user who created the group.
 */
data class Group(
    val createdAt: Instant = Instant.now(),
    val description: String = "",
    val id: String = UUID
        .randomUUID()
        .toString(),
    val name: String = "",
    val user: UserSplitRight = UserSplitRight()
)