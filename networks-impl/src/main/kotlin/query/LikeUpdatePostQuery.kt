package query

import configuration.DbmsInstancesConfiguration
import model.Like
import model.Post
import org.neo4j.driver.Transaction

class LikeUpdatePostQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val id: String,
    private val postId: String
) : Query<Int> {

    override fun invoke(transaction: Transaction): Int {
        return transaction
            .run(
                """
                USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
                MATCH (l:${Like::class.simpleName} {
                    ${Like::id.name}: $${Like::id.name}
                })-[r:A]->(p:${Post::class.simpleName})
                DELETE r
                WITH l
                MATCH (newPost:${Post::class.simpleName} { 
                    ${Post::id.name}: $${::postId.name}
                })
                CREATE (l)-[:A]->(newPost)
                RETURN l
                """.trimIndent(),
                mapOf(
                    Like::id.name to id,
                    ::postId.name to postId
                )
            )
            .list().size
    }
}