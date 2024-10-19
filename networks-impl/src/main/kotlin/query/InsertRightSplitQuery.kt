package query

import configuration.DbmsInstancesConfiguration
import model.*

class InsertRightSplitQuery(
    dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val rightSplit: RightSplit,
    toPrimary: Boolean
) : Query {

    private val composite = dbmsInstancesConfiguration.compositeName

    private val database = if (toPrimary) {
        dbmsInstancesConfiguration.rightSplit.primaryDatabaseName
    } else {
        dbmsInstancesConfiguration.rightSplit.secondaryDatabaseName
    }
    
    override fun cypherize(): List<String> {
        val friendsCypher = rightSplit.friendships.map {
            """
            USE `$composite`.`$database`
            MATCH (u1:${User::class.simpleName} {${UserSplitRight::id.name}: "${it.user1.id}"})
            MATCH (u2:${User::class.simpleName} {${UserSplitRight::id.name}: "${it.user2.id}"})
            CREATE (u1)-[:FRIENDS]-(f:${Friendship::class.simpleName} {
                ${Friendship::id.name}: "${it.id}",
                ${Friendship::since.name}: "${it.since}"
            })-[:WITH]-(u2);
            """.trimIndent()
        }

        val groupsCypher = rightSplit.groups.map {
            """
            USE `$composite`.`$database`
            MATCH (u1:${User::class.simpleName} {${UserSplitRight::id.name}: "${it.user.id}"})
            CREATE (u1)-[:OWNS]->(g:${Group::class.simpleName} {
                ${Group::description.name}: "${it.description}",
                ${Group::createdAt.name}: "${it.createdAt}",
                ${Group::id.name}: "${it.id}",
                ${Group::name.name}: "${it.name}"
            });
            """.trimIndent()
        }

        val membershipsCypher = rightSplit.memberships.map {
            """
            USE `$composite`.`$database`
            MATCH (u1:${User::class.simpleName} {${UserSplitRight::id.name}: "${it.user.id}"})
            MATCH (g:${Group::class.simpleName} {${Group::id.name}: "${it.group.id}"})
            CREATE (u1)-[:HAS]->(ms:${Membership::class.simpleName} {
                ${Membership::id.name}: "${it.id}",
                ${Membership::since.name}: "${it.since}"
            })-[:IN]->(g);
            """.trimIndent()
        }

        val messagesCypher = rightSplit.messages.map {
            """
            USE `$composite`.`$database`
            MATCH (u1:${User::class.simpleName} {${UserSplitRight::id.name}: "${it.userSent.id}"})
            MATCH (u2:${User::class.simpleName} {${UserSplitRight::id.name}: "${it.userReceived.id}"})
            CREATE (u1)-[:SENDS]->(m:${Message::class.simpleName} {
                ${Message::id.name}: "${it.id}",
                ${Message::content.name}: "${it.content}",
                ${Message::sentAt.name}: "${it.sentAt}"
            })-[:TO]->(u2)
            CREATE (u2)-[:RECEIVES]->(m)-[:FROM]->(u1);
            """.trimIndent()
        }

        val usersCypher = rightSplit.users.map {
            """
            USE `$composite`.`$database`
            CREATE (u:${User::class.simpleName} {
                ${UserSplitRight::email.name}: "${it.email}",
                ${UserSplitRight::firstName.name}: "${it.firstName}",
                ${UserSplitRight::id.name}: "${it.id}",
                ${UserSplitRight::lastActiveAt.name}: "${it.lastActiveAt}",
                ${UserSplitRight::lastName.name}: "${it.lastName}"
            });
            """.trimIndent()
        }

        return usersCypher + friendsCypher + messagesCypher + groupsCypher + membershipsCypher
    }
}