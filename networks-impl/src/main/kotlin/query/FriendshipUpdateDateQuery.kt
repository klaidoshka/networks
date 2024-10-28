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
) : QueryNoReturn {

    override fun invoke(transaction: Transaction) {
        val result = transaction.run(
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

        if (!result.hasNext()) {
            throw IllegalStateException("Friendship with id $id not found")
        }

        result.consume()
    }
}