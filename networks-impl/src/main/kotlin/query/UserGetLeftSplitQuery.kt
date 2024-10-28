package query

import configuration.DbmsInstancesConfiguration
import model.UserSplitLeft
import org.neo4j.driver.Transaction
import util.TimeUtil.toInstant
import util.TimeUtil.toLocalDate

class UserGetLeftSplitQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val id: String,
) : Query<UserSplitLeft?> {

    override fun invoke(transaction: Transaction): UserSplitLeft? {
        val result = transaction.run(
            """
            USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
            MATCH (u:User {id: $${::id.name}})
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
                    UserSplitLeft(
                        birthDate = node
                            .get("birthDate")
                            .asString()
                            .toInstant()
                            .toLocalDate(),
                        id = node
                            .get("id")
                            .asString(),
                        interests = node
                            .get("interests")
                            .asList { it.asString() },
                        lastActiveAt = node
                            .get("lastActiveAt")
                            .asString()
                            .toInstant(),
                        location = node
                            .get("location")
                            .asString(),
                        registeredAt = node
                            .get("registeredAt")
                            .asString()
                            .toInstant(),
                        status = node
                            .get("status")
                            .asString(),
                    )
                }
        }

        result.consume()

        return null
    }
}