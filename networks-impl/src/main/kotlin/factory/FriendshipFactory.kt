package factory

import model.Friendship

object FriendshipFactory : Factory<Friendship> {

    override fun create(): Friendship {
        return Friendship()
    }
}