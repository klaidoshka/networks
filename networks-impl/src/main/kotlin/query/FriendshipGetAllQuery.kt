package query

import configuration.DbmsInstancesConfiguration
import model.Friendship
import model.User
import org.neo4j.driver.Transaction

class FriendshipGetAllQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration
) : Query<List<Map<String, Any>>> {

    override fun invoke(transaction: Transaction): List<Map<String, Any>> {
        return transaction
            .run(
                """
                USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
                MATCH (u1:${User::class.simpleName})-[:FRIENDS]->
                    (f:${Friendship::class.simpleName})-[:WITH]->
                    (u2:${User::class.simpleName})
                RETURN 
                    f.${Friendship::id.name} AS friendshipId,
                    u1.${User::id.name} AS userId1,
                    u2.${User::id.name} AS userId2,
                    f.${Friendship::since.name} AS since
                """.trimIndent()
            )
            .list {
                mapOf(
                    "friendshipId" to it["friendshipId"].asString(),
                    "userId1" to it["userId1"].asString(),
                    "userId2" to it["userId2"].asString(),
                    "since" to it["since"].asString()
                )
            }
    }
}