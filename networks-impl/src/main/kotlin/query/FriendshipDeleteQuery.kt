package query

import configuration.DbmsInstancesConfiguration
import model.Friendship
import org.neo4j.driver.Transaction

class FriendshipDeleteQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val id: String,
) : Query<Int> {

    override fun invoke(transaction: Transaction): Int {
        return transaction
            .run(
                """
                USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
                MATCH (f:${Friendship::class.simpleName} {
                    ${Friendship::id.name}: $${::id.name}
                })
                DETACH DELETE f
                RETURN f
                """.trimIndent(),
                mapOf(::id.name to id)
            )
            .list().size
    }
}