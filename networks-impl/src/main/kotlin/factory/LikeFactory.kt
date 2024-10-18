package factory

import model.Like

object LikeFactory : Factory<Like> {

    override fun create(): Like {
        return Like()
    }
}