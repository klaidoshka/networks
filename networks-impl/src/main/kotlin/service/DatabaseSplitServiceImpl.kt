package service

import model.LeftSplit
import model.RightSplit
import model.UserSplitLeft
import model.UserSplitRight
import util.TimeUtil.toLocalDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

class DatabaseSplitServiceImpl(
    private val userService: UserService
) : DatabaseSplitService {

    override suspend fun isInPrimary(
        userId1: String,
        userId2: String,
        date: LocalDate
    ): Boolean {
        return isInPrimary(
            userId = userId1,
            date = date
        ) || isInPrimary(
            userId = userId2,
            date = date
        )
    }

    override suspend fun isInPrimary(
        userId: String,
        date: LocalDate
    ): Boolean {
        val user = userService.getUser(userId) ?: return false

        return isInPrimary(
            user = user.splitToLeft(),
            date = date
        )
    }

    override fun isInPrimary(
        user1: UserSplitLeft,
        user2: UserSplitLeft,
        date: LocalDate
    ): Boolean {
        return isInPrimary(
            user = user1,
            date = date
        ) || isInPrimary(
            user = user2,
            date = date
        )
    }

    override fun isInPrimary(
        user: UserSplitLeft,
        date: LocalDate
    ): Boolean {
        return ChronoUnit.MONTHS.between(
            user.lastActiveAt.toLocalDate(),
            date
        ).absoluteValue < 6
    }

    override fun isInPrimary(
        user1: UserSplitRight,
        user2: UserSplitRight,
        date: LocalDate
    ): Boolean {
        return isInPrimary(
            user = user1,
            date = date
        ) || isInPrimary(
            user = user2,
            date = date
        )
    }

    override fun isInPrimary(
        user: UserSplitRight,
        date: LocalDate
    ): Boolean {
        return ChronoUnit.MONTHS.between(
            user.lastActiveAt.toLocalDate(),
            date
        ).absoluteValue < 6
    }

    override suspend fun split(leftSplit: LeftSplit): Pair<LeftSplit, LeftSplit> {
        val (comments, comments2) = leftSplit.comments.partition {
            isInPrimary(
                user = it.user,
                date = it.commentedAt.toLocalDate()
            )
        }

        val (likes, likes2) = leftSplit.likes.partition {            
            isInPrimary(
                user = it.user,
                date = it.likedAt.toLocalDate()
            )
        }

        val (posts, posts2) = leftSplit.posts.partition {
            isInPrimary(
                user = it.user,
                date = it.postedAt.toLocalDate()
            )
        }

        val now = LocalDate.now()

        val (users, users2) = leftSplit.users.partition {
            isInPrimary(
                user = it,
                date = now
            )
        }

        return LeftSplit(
            comments = comments,
            likes = likes,
            posts = posts,
            users = users
        ) to LeftSplit(
            comments = comments2,
            likes = likes2,
            posts = posts2,
            users = users2
        )
    }

    override suspend fun split(rightSplit: RightSplit): Pair<RightSplit, RightSplit> {
        val (friendships, friendships2) = rightSplit.friendships.partition {
            isInPrimary(
                user1 = it.user1,
                user2 = it.user2,
                date = it.since.toLocalDate()
            )
        }

        val (groups, groups2) = rightSplit.groups.partition {
            isInPrimary(
                user = it.user,
                date = it.createdAt.toLocalDate()
            )
        }

        val (memberships, memberships2) = rightSplit.memberships.partition {
            isInPrimary(
                user = it.user,
                date = it.since.toLocalDate()
            )
        }

        val (messages, messages2) = rightSplit.messages.partition {
            isInPrimary(
                user1 = it.userSent,
                user2 = it.userReceived,
                date = it.sentAt.toLocalDate()
            )
        }

        val now = LocalDate.now()

        val (users, users2) = rightSplit.users.partition {
            isInPrimary(
                user = it,
                date = now
            )
        }

        return RightSplit(
            friendships = friendships,
            groups = groups,
            memberships = memberships,
            messages = messages,
            users = users
        ) to RightSplit(
            friendships = friendships2,
            groups = groups2,
            memberships = memberships2,
            messages = messages2,
            users = users2
        )
    }
}