package query

import configuration.DbmsInstancesConfiguration
import model.Like
import model.Post
import model.User
import model.UserSplitLeft
import org.neo4j.driver.Record
import org.neo4j.driver.Transaction
import util.TimeUtil.toInstant
import java.time.Instant
import java.util.*

class LikeCreateQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val postId: String,
    private val userId: String
) : Query<Like> {

    override fun invoke(transaction: Transaction): Like {
        val result = transaction.run(
            """
            USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
            MATCH
                (p:${Post::class.simpleName} {${Post::id.name}: $${::postId.name}}),
                (u:${User::class.simpleName} {${User::id.name}: $${::userId.name}})
            CREATE (u)-[:LIKES]->(l:${Like::class.simpleName}{
                ${Like::id.name}: $${Like::id.name},
                ${Like::likedAt.name}: $${Like::likedAt.name}
            })-[:A]->(p)
            RETURN l, u, p
            """.trimIndent(),
            mapOf(
                ::postId.name to postId,
                ::userId.name to userId,
                Like::id.name to UUID
                    .randomUUID()
                    .toString(),
                Like::likedAt.name to Instant
                    .now()
                    .toString()
            )
        )

        if (!result.hasNext()) {
            result.consume()

            throw IllegalStateException("Like was not created, post or user might not exist")
        }

        return result
            .single()
            .let(::map)
    }

    private fun map(record: Record): Like {
        val like = record
            .get("l")
            .asNode()

        return Like(
            id = like
                .get(Like::id.name)
                .asString(),
            likedAt = like
                .get(Like::likedAt.name)
                .asString()
                .toInstant(),
            post = Post(
                id = record
                    .get("p")
                    .asNode()
                    .get(Post::id.name)
                    .asString()
            ),
            user = UserSplitLeft(
                id = record
                    .get("u")
                    .asNode()
                    .get(User::id.name)
                    .asString()
            )
        )
    }
}