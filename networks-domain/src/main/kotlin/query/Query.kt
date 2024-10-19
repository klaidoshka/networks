package query

/**
 * Represents a query to be executed.
 */
fun interface Query {

    /**
     * Converts the query to a `cypher` code multiple (if there are many) queries.
     *
     * @return The `cypher` queries.
     */
    fun cypherize(): List<String>
}