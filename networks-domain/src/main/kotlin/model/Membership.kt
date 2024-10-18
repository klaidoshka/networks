package model

import java.time.Instant
import java.util.*

/**
 * Represents a membership of a user in a group.
 *
 * @property group The group the user is a member of.
 * @property id The unique identifier of the membership.
 * @property since The time the membership was established.
 * @property user The user that is a member of the group.
 */
data class Membership(
    val group: Group = Group(),
    val id: String = UUID
        .randomUUID()
        .toString(),
    val since: Instant = Instant.now(),
    val user: UserSplitRight = UserSplitRight()
)