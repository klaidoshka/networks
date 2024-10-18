package query

object DetachDeleteAllQuery : Query {

    override fun cypherize(): List<String> {
        return listOf(
            """
            MATCH (n)
            DETACH DELETE n
            """.trimIndent()
        )
    }
}