package query

import configuration.DbmsInstancesConfiguration
import model.RightSplit

class InsertRightSplitQuery(
    dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val rightSplit: RightSplit,
    toPrimary: Boolean
) : Query {

    private val fabric = dbmsInstancesConfiguration.fabricName

    private val database = if (toPrimary) {
        dbmsInstancesConfiguration.rightSplit.primaryDatabaseName
    } else {
        dbmsInstancesConfiguration.rightSplit.secondaryDatabaseName
    }

    // CREATE (u1)-[:OWNS]->(g)
    // CREATE (u1)-[:FRIENDS]-(f)-[:WITH]-(u2)
    // CREATE (u1)-[:SENDS]->(m)-[:TO]->(u2)
    // CREATE (u2)-[:RECEIVES]->(m)-[:FROM]->(u1)
    // CREATE (u1)-[:HAS]->(ms)-[:IN]->(g);
    override fun cypherize(): List<String> {
        return emptyList()
    }
}