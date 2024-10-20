package query

import configuration.DbmsInstancesConfiguration

class MatchAllQuery(
    private val database: String,
    dbmsInstancesConfiguration: DbmsInstancesConfiguration
) : Query {

    private val composite = dbmsInstancesConfiguration.compositeName

    override fun cypherize(): List<String> {
        return listOf(
            """
            USE `$composite`.`$database`
            MATCH (n)
            RETURN n
            """.trimIndent()
        )
    }
}