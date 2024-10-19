package query

import configuration.DbmsInstancesConfiguration

class DetachDeleteAllQuery(dbmsInstancesConfiguration: DbmsInstancesConfiguration) : Query {

    private val composite = dbmsInstancesConfiguration.compositeName
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
                USE `$composite`.`$database`
                MATCH (n)
                DETACH DELETE n;
                """.trimIndent()
            }
    }
}