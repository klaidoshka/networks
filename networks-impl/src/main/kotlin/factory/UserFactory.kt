package factory

import constant.UserConstants
import model.User
import java.time.Instant
import java.time.LocalDate

object UserFactory : Factory<User> {

    override fun create(): User {
        var user = User()

        user = generateDates(user)
        user = generateInterests(user)
        user = generateInformation(user)

        return user
    }

    private fun generateInformation(user: User): User {
        val firstName = UserConstants.firstNames.random()
        val lastName = UserConstants.lastNames.random()

        return user.copy(
            email = "$firstName.$lastName@${UserConstants.emailDomains.random()}".lowercase(),
            firstName = firstName,
            lastName = lastName,
            location = UserConstants.locations.random(),
            status = UserConstants.statuses.random()
        )
    }

    private fun generateDates(user: User): User {
        val now = Instant.now()
        val registeredAt = now.minusSeconds((1..10000000L).random())
        val lastActiveAt = registeredAt.plusSeconds((1..8000000L).random())

        val birthDate = LocalDate.of(
            (1970..2000).random(),
            (1..12).random(),
            (1..28).random()
        )

        return user.copy(
            birthDate = birthDate,
            lastActiveAt = lastActiveAt,
            registeredAt = registeredAt
        )
    }

    private fun generateInterests(user: User): User {
        val userInterests = mutableSetOf<String>()

        repeat((0..7).random()) {
            userInterests.add(UserConstants.interests.random())
        }

        return user.copy(interests = userInterests.toList())
    }
}