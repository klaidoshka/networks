package query

import configuration.DbmsInstancesConfiguration
import model.Friendship
import org.neo4j.driver.Transaction
import java.time.Instant

class FriendshipUpdateDateQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val id: String,
    private val since: Instant
) : Query<Int> {

    override fun invoke(transaction: Transaction): Int {
        return transaction
            .run(
                """
                USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
                MATCH (f:${Friendship::class.simpleName} {
                    ${Friendship::id.name}: $${Friendship::id.name}
                })
                SET f.${Friendship::since.name} = $${Friendship::since.name}
                RETURN f
                """.trimIndent(),
                mapOf(
                    Friendship::id.name to id,
                    Friendship::since.name to since.toString()
                )
            )
            .list().size
    }
}