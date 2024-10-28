package query

import configuration.DbmsInstancesConfiguration
import model.Friendship
import org.neo4j.driver.Transaction

class FriendshipDeleteQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val id: String,
) : QueryNoReturn {

    override fun invoke(transaction: Transaction) {
        val result = transaction.run(
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
        
        if (!result.hasNext()) {
            throw IllegalArgumentException("Friendship with id $id not found")
        }
        
        result.consume()
    }
}