package query

import configuration.DbmsInstancesConfiguration

class DetachDeleteAllQuery(
    private val database: String,
    dbmsInstancesConfiguration: DbmsInstancesConfiguration
) : Query {

    private val composite = dbmsInstancesConfiguration.compositeName

    override fun cypherize(): List<String> {
        return listOf(
            """
            USE `$composite`.`$database`
            MATCH (n)
            DETACH DELETE n;
            """.trimIndent()
        )
    }
}