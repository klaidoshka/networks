package query

import configuration.DbmsInstancesConfiguration
import model.*
import org.neo4j.driver.Query
import org.neo4j.driver.Transaction

class InsertLeftSplitQuery(
    dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val leftSplit: LeftSplit,
    toPrimary: Boolean
) : QueryNoReturn {

    private val composite = dbmsInstancesConfiguration.compositeName

    private val database = if (toPrimary) {
        dbmsInstancesConfiguration.leftSplit.primaryDatabaseName
    } else {
        dbmsInstancesConfiguration.leftSplit.secondaryDatabaseName
    }

    private val query by lazy {
        val commentContentPlaceholder = "\$commentContent"
        val commentIdPlaceholder = "\$commentId"
        val commentPostIdPlaceholder = "\$commentPostId"
        val commentUserIdPlaceholder = "\$commentUserId"

        val commentsCypher = leftSplit.comments.mapIndexed { index, it ->
            """
            MATCH (cU$index:${User::class.simpleName} {${User::id.name}: $commentUserIdPlaceholder$index})
            MATCH (cP$index:${Post::class.simpleName} {${Post::id.name}: $commentPostIdPlaceholder$index})
            CREATE (c$index:${Comment::class.simpleName} {
                ${Comment::id.name}: $commentIdPlaceholder$index,
                ${Comment::content.name}: $commentContentPlaceholder$index,
                ${Comment::commentedAt.name}: $${it::commentedAt.name}$index
            })
            CREATE (cU$index)-[:COMMENTS]->(c$index)-[:ON]->(cP$index)
            WITH c$index, cU$index, cP$index
            """.trimIndent() to mapOf(
                commentUserIdPlaceholder + index to it.user.id,
                commentPostIdPlaceholder + index to it.post.id,
                commentIdPlaceholder + index to it.id,
                commentContentPlaceholder + index to it.content,
                it::commentedAt.name + index to it.commentedAt.toString()
            )
        }

        val likeIdPlaceholder = "\$likeId"
        val likePostIdPlaceholder = "\$likePostId"
        val likeUserIdPlaceholder = "\$likeUserId"

        val likesCypher = leftSplit.likes.mapIndexed { index, it ->
            """
            MATCH (lU$index:${User::class.simpleName} {${User::id.name}: $likeUserIdPlaceholder$index})
            MATCH (lP$index:${Post::class.simpleName} {${Post::id.name}: $likePostIdPlaceholder$index})
            CREATE (l$index:${Like::class.simpleName} {
                ${Like::id.name}: $likeIdPlaceholder$index,
                ${Like::likedAt.name}: $${it::likedAt.name}$index
            })
            CREATE (lU$index)-[:LIKES]->(l$index)-[:A]->(lP$index)
            WITH l$index, lU$index, lP$index
            """.trimIndent() to mapOf(
                likeUserIdPlaceholder + index to it.user.id,
                likePostIdPlaceholder + index to it.post.id,
                likeIdPlaceholder + index to it.id,
                it::likedAt.name + index to it.likedAt.toString()
            )
        }

        val postContentPlaceholder = "\$postContent"
        val postIdPlaceholder = "\$postId"
        val postUserIdPlaceholder = "\$postUserId"

        val postsCypher = leftSplit.posts.mapIndexed { index, it ->
            """
            MATCH (pU$index:${User::class.simpleName} {${User::id.name}: $postUserIdPlaceholder$index})
            CREATE (p$index:${Post::class.simpleName} {
                ${Post::id.name}: $postIdPlaceholder$index,
                ${Post::content.name}: $postContentPlaceholder$index,
                ${Post::postedAt.name}: $${it::postedAt.name}$index
            })
            CREATE (pU$index)-[:CREATES]->(p$index)
            WITH p$index, pU$index
            """.trimIndent() to mapOf(
                postUserIdPlaceholder + index to it.user.id,
                postIdPlaceholder + index to it.id,
                postContentPlaceholder + index to it.content,
                it::postedAt.name + index to it.postedAt.toString()
            )
        }

        val userIdPlaceholder = "\$userId"
        val userBirthDatePlaceholder = "\$userBirthDate"
        val userInterestsPlaceholder = "\$userInterests"
        val userLastActiveAtPlaceholder = "\$userLastActiveAt"
        val userLocationPlaceholder = "\$userLocation"
        val userRegisteredAtPlaceholder = "\$userRegisteredAt"
        val userStatusPlaceholder = "\$userStatus"

        val usersCypher = leftSplit.users.mapIndexed { index, it ->
            """
            CREATE (u$index:${User::class.simpleName} {
                ${User::id.name}: $userIdPlaceholder$index,
                ${User::birthDate.name}: $userBirthDatePlaceholder$index,
                ${User::interests.name}: $userInterestsPlaceholder$index,
                ${User::lastActiveAt.name}: $userLastActiveAtPlaceholder$index,
                ${User::location.name}: $userLocationPlaceholder$index,
                ${User::registeredAt.name}: $userRegisteredAtPlaceholder$index,
                ${User::status.name}: $userStatusPlaceholder$index
            })
            WITH u$index
            """.trimIndent() to mapOf(
                userIdPlaceholder + index to it.id,
                userBirthDatePlaceholder + index to it.birthDate.toString(),
                userInterestsPlaceholder + index to it.interests,
                userLastActiveAtPlaceholder + index to it.lastActiveAt.toString(),
                userLocationPlaceholder + index to it.location,
                userRegisteredAtPlaceholder + index to it.registeredAt.toString(),
                userStatusPlaceholder + index to it.status
            )
        }

        val queries = usersCypher + postsCypher + commentsCypher + likesCypher

        if (queries.isEmpty()) {
            return@lazy Query("RETURN 0;")
        }

        val query = "USE `$composite`.`$database`\n" + queries
            .joinToString("\n") { it.first }
            .lines()
            .dropLast(1)
            .joinToString("\n") + ";"

        val params = queries
            .fold(mapOf<String, Any>()) { acc, it -> acc + it.second }
            .mapKeys { it.key.removePrefix("$") }

        Query(
            query,
            params
        )
    }

    override fun invoke(transaction: Transaction) {
        transaction
            .run(query)
            .consume()
    }
}