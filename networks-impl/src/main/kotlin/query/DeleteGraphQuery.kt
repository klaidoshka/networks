package query

import configuration.DbmsInstancesConfiguration
import org.neo4j.driver.Transaction

class DeleteGraphQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration
) : QueryNoReturn {

    override fun invoke(transaction: Transaction) {
        transaction
            .run(
                """
                USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
                MATCH (n)
                DETACH DELETE n;
                """.trimIndent()
            )
            .consume()
    }
}