package query

/**
 * Represents a query to be executed.
 * 
 * TODO: Use injected parameters, return a Map of parameters to use with the query.
 */
fun interface Query {

    /**
     * Converts the query to a `cypher` code multiple (if there are many) queries.
     *
     * @return The `cypher` queries.
     */
    fun cypherize(): List<String>
}