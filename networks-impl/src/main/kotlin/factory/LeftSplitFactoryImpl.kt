package factory

import model.*
import util.TimeUtil.increaseRandomlyUpTo

class LeftSplitFactoryImpl(
    private val commentFactory: Factory<Comment>,
    private val likeFactory: Factory<Like>,
    private val postFactory: Factory<Post>
) : LeftSplitFactory {

    override fun create(users: List<User>): LeftSplit {
        val usersSplit = users.map { it.splitToLeft() }

        val posts = postFactory
            .create(users.size)
            .map {
                val user = usersSplit.random()

                it.copy(
                    postedAt = user.registeredAt.increaseRandomlyUpTo(user.lastActiveAt),
                    user = user
                )
            }
            .toMutableList()

        val comments = commentFactory
            .create(users.size)
            .map {
                val post = posts.random()
                val user = usersSplit.random()

                val postNew = post.copy(
                    comments = post.comments + it
                )

                posts[posts.indexOf(post)] = postNew

                it.copy(
                    commentedAt = postNew.postedAt.increaseRandomlyUpTo(user.lastActiveAt),
                    post = postNew,
                    user = user
                )
            }

        val likes = likeFactory
            .create(users.size)
            .map {
                val post = posts.random()
                val user = usersSplit.random()

                val postNew = post.copy(
                    likes = post.likes + it
                )

                posts[posts.indexOf(post)] = postNew

                it.copy(
                    likedAt = postNew.postedAt.increaseRandomlyUpTo(user.lastActiveAt),
                    post = postNew,
                    user = user
                )
            }

        return LeftSplit(
            comments = comments,
            likes = likes,
            posts = posts,
            users = usersSplit
        )
    }
}