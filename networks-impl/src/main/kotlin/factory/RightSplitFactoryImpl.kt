package factory

import model.*

class RightSplitFactoryImpl(
    private val friendshipFactory: Factory<Friendship>,
    private val groupFactory: Factory<Group>,
    private val membershipFactory: Factory<Membership>,
    private val messageFactory: Factory<Message>
) : RightSplitFactory {

    override fun create(users: List<User>): RightSplit {
        val usersSplit = users.map { it.splitToRight() }

        val friendships = if (users.size < 2) emptyList() else friendshipFactory
            .create(users.size)
            .map {
                val user1 = usersSplit.random()
                var user2: UserSplitRight

                do {
                    user2 = usersSplit.random()
                } while (user1 == user2)

                it.copy(
                    since = users
                        .first { u -> user1.id == u.id }
                        .adjust(
                            users
                                .first { u -> user2.id == u.id }
                                .adjust(it.since)
                        ),
                    user1 = user1,
                    user2 = user2
                )
            }

        val messages = if (users.size < 2) emptyList() else messageFactory
            .create(users.size)
            .map {
                val sender = usersSplit.random()
                var receiver: UserSplitRight

                do {
                    receiver = usersSplit.random()
                } while (sender == receiver)

                it.copy(
                    sentAt = users
                        .first { u -> sender.id == u.id }
                        .adjust(it.sentAt),
                    userReceived = receiver,
                    userSent = sender
                )
            }

        val groups = groupFactory
            .create(users.size)
            .map {
                val user = usersSplit.random()

                it.copy(
                    createdAt = users
                        .first { u -> user.id == u.id }
                        .adjust(it.createdAt)
                        .minusSeconds(3600 * 24 * 14),
                    user = usersSplit.random()
                )
            }

        val memberships = membershipFactory
            .create(users.size)
            .zip(groups)
            .map { (membership, group) ->
                membership.copy(
                    group = group,
                    since = group.createdAt,
                    user = group.user
                )
            } + membershipFactory
            .create(users.size / 2)
            .mapNotNull {
                val group = groups.random()

                var member: UserSplitRight

                do {
                    member = usersSplit.random()
                } while (member == group.user)

                if (member.lastActiveAt.isBefore(group.createdAt)) {
                    return@mapNotNull null
                }

                it.copy(
                    group = group,
                    since = group.createdAt.plusSeconds(3600 * 24),
                    user = member
                )
            }

        return RightSplit(
            friendships = friendships,
            groups = groups,
            memberships = memberships,
            messages = messages,
            users = usersSplit
        )
    }
}