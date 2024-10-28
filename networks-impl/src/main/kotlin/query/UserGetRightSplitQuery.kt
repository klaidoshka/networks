package query

import configuration.DbmsInstancesConfiguration
import model.User
import model.UserSplitRight
import org.neo4j.driver.Transaction
import util.TimeUtil.toInstant

class UserGetRightSplitQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val id: String,
) : Query<UserSplitRight?> {

    override fun invoke(transaction: Transaction): UserSplitRight? {
        val result = transaction.run(
            """
            USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
            MATCH (u:${User::class.simpleName} {${User::id.name}: ${'$'}${::id.name}})
            RETURN u
            """.trimIndent(),
            mapOf(::id.name to id)
        )

        if (result.hasNext()) {
            return result
                .single()
                .get("u")
                .asNode()
                .let { node ->
                    UserSplitRight(
                        email = node
                            .get("email")
                            .asString(),
                        firstName = node
                            .get("firstName")
                            .asString(),
                        id = node
                            .get("id")
                            .asString(),
                        lastActiveAt = node
                            .get("lastActiveAt")
                            .asString()
                            .toInstant(),
                        lastName = node
                            .get("lastName")
                            .asString()
                    )
                }
        }

        result.consume()

        return null
    }
}