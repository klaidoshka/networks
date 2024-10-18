package model

import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * User entity split part into "left" database.
 *
 * @property birthDate The birthdate of the user.
 * @property id The unique identifier of the user.
 * @property interests The interests of the user.
 * @property lastActiveAt The last active date of the user.
 * @property location The location of the user.
 * @property registeredAt The registration date of the user.
 * @property status The status of the user.
 */
data class UserSplitLeft(
    val birthDate: LocalDate = LocalDate.now(),
    val id: String = UUID
        .randomUUID()
        .toString(),
    val interests: List<String> = emptyList(),
    val lastActiveAt: Instant = Instant.now(),
    val location: String = "",
    val registeredAt: Instant = Instant.now(),
    val status: String = ""
)

/**
 * User entity split part into "right" database.
 *
 * @property email The email of the user.
 * @property firstName The first name of the user.
 * @property id The unique identifier of the user.
 * @property lastActiveAt The last active date of the user.
 * @property lastName The last name of the user.
 */
data class UserSplitRight(
    val email: String = "",
    val firstName: String = "",
    val id: String = UUID
        .randomUUID()
        .toString(),
    val lastActiveAt: Instant = Instant.now(),
    val lastName: String = ""
)

/**
 * User entity joined from two split entities in separate databases.
 *
 * @property birthDate The birthdate of the user.
 * @property email The email of the user.
 * @property firstName The first name of the user.
 * @property id The unique identifier of the user.
 * @property interests The interests of the user.
 * @property lastActiveAt The last active date of the user.
 * @property lastName The last name of the user.
 * @property location The location of the user.
 * @property registeredAt The registration date of the user.
 * @property status The status of the user.
 */
data class User(
    val birthDate: LocalDate = LocalDate.now(),
    val email: String = "",
    val firstName: String = "",
    val id: String = UUID
        .randomUUID()
        .toString(),
    val interests: List<String> = emptyList(),
    val lastActiveAt: Instant = Instant.now(),
    val lastName: String = "",
    val location: String = "",
    val registeredAt: Instant = Instant.now(),
    val status: String = ""
) {

    /**
     * Adjust the instant to be within the bounds of the user entity's registration date and
     * last active date. It is expected that the registration date is before the last active date.
     *
     * @param instant The instant to adjust.
     *
     * @return The adjusted instant.
     */
    fun adjust(instant: Instant): Instant {
        if (instant.isBefore(this.registeredAt)) {
            return this.registeredAt.plusSeconds(30)
        }

        if (instant.isAfter(this.lastActiveAt)) {
            return this.lastActiveAt.minusSeconds(10)
        }

        return instant
    }

    /**
     * Split the user entity into the left part.
     *
     * @return The left part of the user entity.
     */
    fun splitToLeft(): UserSplitLeft {
        return UserSplitLeft(
            birthDate = birthDate,
            id = id,
            interests = interests,
            lastActiveAt = lastActiveAt,
            location = location,
            registeredAt = registeredAt,
            status = status
        )
    }

    /**
     * Split the user entity into the right part.
     *
     * @return The right part of the user entity.
     */
    fun splitToRight(): UserSplitRight {
        return UserSplitRight(
            email = email,
            firstName = firstName,
            id = id,
            lastActiveAt = lastActiveAt,
            lastName = lastName
        )
    }
}