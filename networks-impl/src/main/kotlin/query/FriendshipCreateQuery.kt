package query

import configuration.DbmsInstancesConfiguration
import model.Friendship
import model.User
import model.UserSplitRight
import org.neo4j.driver.Record
import org.neo4j.driver.Transaction
import java.time.Instant
import java.util.*

class FriendshipCreateQuery(
    private val database: String,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val userId1: String,
    private val userId2: String,
    private val since: Instant
) : Query<Friendship> {

    override fun invoke(transaction: Transaction): Friendship {
        val result = transaction.run(
            """
            USE `${dbmsInstancesConfiguration.compositeName}`.`$database`
            MATCH
                (u1:${User::class.simpleName} {
                    ${User::id.name}: $${::userId1.name}
                }),
                (u2:${User::class.simpleName} {
                    ${User::id.name}: $${::userId2.name}
                })
            CREATE (u1)-[:FRIENDS]->(f:${Friendship::class.simpleName} {
                ${Friendship::id.name}: $${Friendship::id.name},
                ${Friendship::since.name}: $${::since.name}
            })-[:WITH]->(u2)
            RETURN f
            """.trimIndent(),
            mapOf(
                ::userId1.name to userId1,
                ::userId2.name to userId2,
                Friendship::id.name to UUID
                    .randomUUID()
                    .toString(),
                ::since.name to since.toString()
            )
        )

        if (result.hasNext()) {
            return map(result.single())
        } else {
            result.consume()

            throw IllegalStateException("Friendship was not created, users might not exist")
        }
    }

    private fun map(record: Record): Friendship {
        return Friendship(
            id = record
                .values()
                .first()[Friendship::id.name].asString(),
            since = Instant.parse(
                record
                    .values()
                    .first()[Friendship::since.name]
                    .asString()
            ),
            user1 = UserSplitRight(id = userId1),
            user2 = UserSplitRight(id = userId2)
        )
    }
}