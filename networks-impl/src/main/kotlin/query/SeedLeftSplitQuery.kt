package query

import model.*

class SeedLeftSplitQuery(
    private val leftSplit: LeftSplit
) : Query {

    // CREATE (u)-[:CREATES]->(p)
    // CREATE (u)-[:LIKES]->(l)-[:ON]->(p)
    // CREATE (u)-[:COMMENTS]->(c)-[:ON]->(p);
    override fun cypherize(): List<String> {
        val users = leftSplit.users
        val posts = leftSplit.posts
        val comments = leftSplit.comments
        val likes = leftSplit.likes

        val usersCypher = users.map {
            """
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

        val postsCypher = posts.map {
            """
            MATCH (u:${User::class.simpleName} {${User::id.name}: "${it.user.id}"})
            CREATE (p:${Post::class.simpleName} {
                ${Post::id.name}: "${it.id}",
                ${Post::content.name}: "${it.content}",
                ${Post::postedAt.name}: "${it.postedAt}"
            })
            CREATE (u)-[:CREATES]->(p);
            """.trimIndent()
        }

        val commentsCypher = comments.map {
            """
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

        val likesCypher = likes.map {
            """
            MATCH (u:${User::class.simpleName} {${User::id.name}: "${it.user.id}"})
            MATCH (p:${Post::class.simpleName} {${Post::id.name}: "${it.post.id}"})
            CREATE (l:${Like::class.simpleName} {
                ${Like::id.name}: "${it.id}",
                ${Like::likedAt.name}: "${it.likedAt}"
            })
            CREATE (u)-[:LIKED]->(l)-[:A]->(p);
            """.trimIndent()
        }

        return usersCypher + postsCypher + commentsCypher + likesCypher
    }
}