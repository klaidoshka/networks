package query

/**
 * Represents a query to be executed.
 */
fun interface Query {

    /**
     * Converts the query to a Cypher queries.
     *
     * @return The Cypher queries to run in Neo4j.
     */
    fun cypherize(): List<String>
}