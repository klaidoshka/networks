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
            MATCH ($NODE_KEY)-[$RELATIONSHIP_KEY]-($RELATED_NODE_KEY)
            RETURN DISTINCT $NODE_KEY, $RELATIONSHIP_KEY, $RELATED_NODE_KEY
            """.trimIndent()
        )
    }

    companion object {

        const val NODE_KEY = "n"
        const val RELATED_NODE_KEY = "m"
        const val RELATIONSHIP_KEY = "r"
    }
}