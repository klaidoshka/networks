package model

import java.time.Instant
import java.util.*

/**
 * Represents a message sent by a user.
 *
 * @property content The content of the message.
 * @property id The unique identifier of the message.
 * @property sentAt The time the message was sent.
 * @property userReceived The user who received the message.
 * @property userSent The user who sent the message.
 */
data class Message(
    val content: String,
    val id: String = UUID
        .randomUUID()
        .toString(),
    val sentAt: Instant = Instant.now(),
    val userReceived: UserSplitRight = UserSplitRight(),
    val userSent: UserSplitRight = UserSplitRight()
)