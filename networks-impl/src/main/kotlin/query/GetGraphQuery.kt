package query

import configuration.DbmsInstancesConfiguration
import org.neo4j.driver.Transaction
import org.neo4j.driver.types.Node
import org.neo4j.driver.types.Relationship

class GetGraphQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration
) : Query<List<Triple<Node, Relationship, Node>>> {

    override fun invoke(transaction: Transaction): List<Triple<Node, Relationship, Node>> {
        return transaction
            .run(
                """
                USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
                MATCH (n)-[r]-(m)
                RETURN DISTINCT n, r, m
                """.trimIndent()
            )
            .list {
                Triple(
                    it["n"].asNode(),
                    it["r"].asRelationship(),
                    it["m"].asNode()
                )
            }
    }
}