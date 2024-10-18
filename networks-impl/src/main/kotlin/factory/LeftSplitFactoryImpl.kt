package factory

import model.*

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
                    postedAt = users
                        .first { u -> user.id == u.id }
                        .adjust(it.postedAt),
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
                    commentedAt = users
                        .first { u -> user.id == u.id }
                        .adjust(it.commentedAt),
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
                    likedAt = users
                        .first { u -> user.id == u.id }
                        .adjust(it.likedAt),
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