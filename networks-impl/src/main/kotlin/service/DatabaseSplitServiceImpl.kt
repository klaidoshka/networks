package service

import model.LeftSplit
import model.RightSplit
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DatabaseSplitServiceImpl : DatabaseSplitService {

    override fun isInPrimary(
        userId1: String,
        userId2: String,
        date: LocalDate
    ): Boolean {
        return date.dayOfMonth % 2 == 0
    }

    override fun isInPrimary(
        user: String,
        date: LocalDate
    ) {
        TODO("Not yet implemented")
    }

    override fun split(leftSplit: LeftSplit): Pair<LeftSplit, LeftSplit> {
        val now = Instant.now()

        val (comments, comments2) = leftSplit.comments.partition { it.commentedAt.isWithin6Months(now) }
        val (likes, likes2) = leftSplit.likes.partition { it.likedAt.isWithin6Months(now) }
        val (posts, posts2) = leftSplit.posts.partition { it.postedAt.isWithin6Months(now) }
        val (users, users2) = leftSplit.users.partition { it.lastActiveAt.isWithin6Months(now) }

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

    override fun split(rightSplit: RightSplit): Pair<RightSplit, RightSplit> {
        val now = Instant.now()

        val (friendships, friendships2) = rightSplit.friendships.partition { it.since.isWithin6Months(now) }
        val (groups, groups2) = rightSplit.groups.partition { it.createdAt.isWithin6Months(now) }
        val (memberships, memberships2) = rightSplit.memberships.partition { it.since.isWithin6Months(now) }
        val (messages, messages2) = rightSplit.messages.partition { it.sentAt.isWithin6Months(now) }
        val (users, users2) = rightSplit.users.partition { it.lastActiveAt.isWithin6Months(now) }

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

    /**
     * Check if the instant is within 6 months.
     *
     * @param now The current instant.
     *
     * @return True if the instant is within 6 months, false otherwise.
     */
    fun Instant.isWithin6Months(now: Instant) = this > now.minus(
        6 * 30,
        ChronoUnit.DAYS
    )
}