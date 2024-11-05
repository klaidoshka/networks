package query

import configuration.DbmsInstancesConfiguration
import model.Like
import model.Post
import model.User
import model.UserSplitLeft
import org.neo4j.driver.Record
import org.neo4j.driver.Transaction
import util.TimeUtil.toInstant

class LikeGetAllQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration
) : Query<List<Like>> {

    override fun invoke(transaction: Transaction): List<Like> {
        // TODO: Rethink the logic for this. Maybe more correct filter can be applied to ignore link-nodes.
        return transaction
            .run(
                """
                USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
                MATCH
                    (u:${User::class.simpleName})-[:LIKES]->
                    (l:${Like::class.simpleName})-[:A]->
                    (p:${Post::class.simpleName})
                WHERE size(keys(u)) > 1 AND size(keys(l)) > 1 AND size(keys(p)) > 1
                RETURN l, u, p
                """.trimIndent()
            )
            .list(::map)
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