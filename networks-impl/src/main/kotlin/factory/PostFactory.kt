package factory

import constant.PostConstants
import model.Post

object PostFactory : Factory<Post> {

    override fun create(): Post {
        return Post(
            content = PostConstants.contents.random()
        )
    }
}