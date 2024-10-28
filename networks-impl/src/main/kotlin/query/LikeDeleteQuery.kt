package query

import configuration.DbmsInstancesConfiguration
import model.Like
import org.neo4j.driver.Transaction

class LikeDeleteQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val id: String,
) : Query<Int> {

    override fun invoke(transaction: Transaction): Int {
        return transaction
            .run(
                """
                USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
                MATCH (l:${Like::class.simpleName} {
                    ${Like::id.name}: $${::id.name}
                })
                DETACH DELETE l
                RETURN l
                """.trimIndent(),
                mapOf(::id.name to id)
            )
            .list().size
    }
}