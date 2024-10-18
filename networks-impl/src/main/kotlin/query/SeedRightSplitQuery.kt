package query

import model.RightSplit

class SeedRightSplitQuery(
    private val rightSplit: RightSplit
) : Query {

    // CREATE (u1)-[:JOINS]->(g)
    // CREATE (u1)-[:FRIENDS]->(f)-[:WITH]->(u2)
    // CREATE (u2)-[:FRIENDS]->(f)-[:WITH]->(u1)
    // CREATE (u1)-[:SENDS]->(m)-[:TO]->(u2)
    // CREATE (u2)-[:RECEIVES]->(m)-[:FROM]->(u1)
    // CREATE (u1)-[:MEMBER_OF]->(ms)-[:IN]->(g);
    override fun cypherize(): List<String> {
//        TODO("Not yet implemented")
        return emptyList()
    }
}