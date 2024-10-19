package query

import configuration.DbmsInstancesConfiguration
import model.*

class InsertLeftSplitQuery(
    dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val leftSplit: LeftSplit,
    toPrimary: Boolean
) : Query {

    private val fabric = dbmsInstancesConfiguration.fabricName

    private val database = if (toPrimary) {
        dbmsInstancesConfiguration.leftSplit.primaryDatabaseName
    } else {
        dbmsInstancesConfiguration.leftSplit.secondaryDatabaseName
    }

    override fun cypherize(): List<String> {
        val commentsCypher = leftSplit.comments.map {
            """
            USE $fabric.$database
            MATCH (u:${User::class.simpleName} {${User::id.name}: "${it.user.id}"})
            MATCH (p:${Post::class.simpleName} {${Post::id.name}: "${it.post.id}"})
            CREATE (c:${Comment::class.simpleName} {
                ${Comment::id.name}: "${it.id}",
                ${Comment::content.name}: "${it.content}",
                ${Comment::commentedAt.name}: "${it.commentedAt}"
            })
            CREATE (u)-[:COMMENTS]->(c)-[:ON]->(p);
            """.trimIndent()
        }

        val likesCypher = leftSplit.likes.map {
            """
            USE $fabric.$database
            MATCH (u:${User::class.simpleName} {${User::id.name}: "${it.user.id}"})
            MATCH (p:${Post::class.simpleName} {${Post::id.name}: "${it.post.id}"})
            CREATE (l:${Like::class.simpleName} {
                ${Like::id.name}: "${it.id}",
                ${Like::likedAt.name}: "${it.likedAt}"
            })
            CREATE (u)-[:LIKES]->(l)-[:A]->(p);
            """.trimIndent()
        }

        val postsCypher = leftSplit.posts.map {
            """
            USE $fabric.$database
            MATCH (u:${User::class.simpleName} {${User::id.name}: "${it.user.id}"})
            CREATE (p:${Post::class.simpleName} {
                ${Post::id.name}: "${it.id}",
                ${Post::content.name}: "${it.content}",
                ${Post::postedAt.name}: "${it.postedAt}"
            })
            CREATE (u)-[:CREATES]->(p);
            """.trimIndent()
        }
        val usersCypher = leftSplit.users.map {
            """
            USE $fabric.$database
            CREATE (u:${User::class.simpleName} {
                ${User::id.name}: "${it.id}",
                ${User::birthDate.name}: "${it.birthDate}",
                ${User::interests.name}: ${
                it.interests.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) { interest -> "\"$interest\"" }
            },
                ${User::lastActiveAt.name}: "${it.lastActiveAt}",
                ${User::location.name}: "${it.location}",
                ${User::registeredAt.name}: "${it.registeredAt}",
                ${User::status.name}: "${it.status}"
            });
            """.trimIndent()
        }

        return usersCypher + postsCypher + commentsCypher + likesCypher
    }
}