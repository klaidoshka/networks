package factory

import model.*

class RightSplitFactoryImpl(
    private val friendshipFactory: Factory<Friendship>,
    private val groupFactory: Factory<Group>,
    private val membershipFactory: Factory<Membership>,
    private val messageFactory: Factory<Message>
) : RightSplitFactory {

    override fun create(users: List<User>): RightSplit {
        return RightSplit(
            friendships = emptyList(),
            groups = emptyList(),
            memberships = emptyList(),
            messages = emptyList(),
            users = users.map { it.splitToRight() }
        )
    }
}