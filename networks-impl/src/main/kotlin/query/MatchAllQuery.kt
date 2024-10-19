package query

import configuration.DbmsInstancesConfiguration

class MatchAllQuery(dbmsInstancesConfiguration: DbmsInstancesConfiguration) : Query {

    private val fabric = dbmsInstancesConfiguration.fabricName
    private val db11 = dbmsInstancesConfiguration.leftSplit.primaryDatabaseName
    private val db12 = dbmsInstancesConfiguration.leftSplit.secondaryDatabaseName
    private val db21 = dbmsInstancesConfiguration.rightSplit.primaryDatabaseName
    private val db22 = dbmsInstancesConfiguration.rightSplit.secondaryDatabaseName

    override fun cypherize(): List<String> {
        return listOf(
            db11,
            db12,
            db21,
            db22
        )
            .map { database ->
                """
                USE $fabric.$database
                MATCH (n)
                RETURN n
                """.trimIndent()
            }
    }
}