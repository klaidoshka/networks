package query

import configuration.DbmsInstancesConfiguration
import model.*
import org.neo4j.driver.Query
import org.neo4j.driver.Transaction

class InsertRightSplitQuery(
    dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val rightSplit: RightSplit,
    toPrimary: Boolean
) : QueryNoReturn {

    private val composite = dbmsInstancesConfiguration.compositeName

    private val database = if (toPrimary) {
        dbmsInstancesConfiguration.rightSplit.primaryDatabaseName
    } else {
        dbmsInstancesConfiguration.rightSplit.secondaryDatabaseName
    }

    private val query by lazy {
        val friendshipIdPlaceholder = "\$friendshipId"
        val friendshipSincePlaceholder = "\$friendshipSince"
        val friendshipUser1IdPlaceholder = "\$friendshipUser1Id"
        val friendshipUser2IdPlaceholder = "\$friendshipUser2Id"

        val friendsCypher = rightSplit.friendships.mapIndexed { index, it ->
            """
            MATCH (fU1$index:${User::class.simpleName} {${UserSplitRight::id.name}: $friendshipUser1IdPlaceholder$index})
            MATCH (fU2$index:${User::class.simpleName} {${UserSplitRight::id.name}: $friendshipUser2IdPlaceholder$index})
            CREATE (fU1$index)-[:FRIENDS]->(f$index:${Friendship::class.simpleName} {
                ${Friendship::id.name}: $friendshipIdPlaceholder$index,
                ${Friendship::since.name}: $friendshipSincePlaceholder$index
            })-[:WITH]->(fU2$index)
            WITH f$index, fU1$index, fU2$index
            """.trimIndent() to mapOf(
                friendshipIdPlaceholder + index to it.id,
                friendshipSincePlaceholder + index to it.since.toString(),
                friendshipUser1IdPlaceholder + index to it.user1.id,
                friendshipUser2IdPlaceholder + index to it.user2.id
            )
        }

        val groupGroupIdPlaceholder = "\$groupGroupId"
        val groupUserIdPlaceholder = "\$groupUserId"

        val groupsCypher = rightSplit.groups.mapIndexed { index, it ->
            """
            MATCH (gU1$index:${User::class.simpleName} {${UserSplitRight::id.name}: $groupUserIdPlaceholder$index})
            CREATE (gU1$index)-[:OWNS]->(g$index:${Group::class.simpleName} {
                ${Group::description.name}: $${it::description.name}$index,
                ${Group::createdAt.name}: $${it::createdAt.name}$index,
                ${Group::id.name}: $groupGroupIdPlaceholder$index,
                ${Group::name.name}: $${it::name.name}$index
            })
            WITH g$index, gU1$index
            """.trimIndent() to mapOf(
                groupGroupIdPlaceholder + index to it.id,
                groupUserIdPlaceholder + index to it.user.id,
                it::createdAt.name + index to it.createdAt.toString(),
                it::description.name + index to it.description,
                it::name.name + index to it.name
            )
        }

        val membershipIdPlaceholder = "\$membershipId"
        val membershipGroupIdPlaceholder = "\$membershipGroupId"
        val membershipSincePlaceholder = "\$membershipSince"
        val membershipUserIdPlaceholder = "\$membershipUserId"

        val membershipsCypher = rightSplit.memberships.mapIndexed { index, it ->
            """
            MATCH (msU$index:${User::class.simpleName} {${UserSplitRight::id.name}: $membershipUserIdPlaceholder$index})
            MATCH (msG$index:${Group::class.simpleName} {${Group::id.name}: $membershipGroupIdPlaceholder$index})
            CREATE (msU$index)-[:HAS]->(ms$index:${Membership::class.simpleName} {
                ${Membership::id.name}: $membershipIdPlaceholder$index,
                ${Membership::since.name}: $membershipSincePlaceholder$index
            })-[:IN]->(msG$index)
            WITH ms$index, msU$index, msG$index
            """.trimIndent() to mapOf(
                membershipIdPlaceholder + index to it.id,
                membershipSincePlaceholder + index to it.since.toString(),
                membershipUserIdPlaceholder + index to it.user.id,
                membershipGroupIdPlaceholder + index to it.group.id
            )
        }

        val messageContentPlaceholder = "\$messageContent"
        val messageIdPlaceholder = "\$messageId"
        val messageUserReceivedIdPlaceholder = "\$messageUserReceivedId"
        val messageUserSentIdPlaceholder = "\$messageUserSentId"

        val messagesCypher = rightSplit.messages.mapIndexed { index, it ->
            """
            MATCH (mU1$index:${User::class.simpleName} {${UserSplitRight::id.name}: $messageUserSentIdPlaceholder$index})
            MATCH (mU2$index:${User::class.simpleName} {${UserSplitRight::id.name}: $messageUserReceivedIdPlaceholder$index})
            CREATE (mU1$index)-[:SENDS]->(m$index:${Message::class.simpleName} {
                ${Message::id.name}: $messageIdPlaceholder$index,
                ${Message::content.name}: $messageContentPlaceholder$index,
                ${Message::sentAt.name}: $${it::sentAt.name}$index
            })-[:TO]->(mU2$index)
            CREATE (mU2$index)-[:RECEIVES]->(m$index)-[:FROM]->(mU1$index)
            WITH m$index, mU1$index, mU2$index
            """.trimIndent() to mapOf(
                messageContentPlaceholder + index to it.content,
                messageIdPlaceholder + index to it.id,
                messageUserReceivedIdPlaceholder + index to it.userReceived.id,
                messageUserSentIdPlaceholder + index to it.userSent.id,
                it::sentAt.name + index to it.sentAt.toString()
            )
        }

        val userIdPlaceholder = "\$userId"

        val usersCypher = rightSplit.users.mapIndexed { index, it ->
            """
            CREATE (u$index:${User::class.simpleName} {
                ${UserSplitRight::email.name}: $${it::email.name}$index,
                ${UserSplitRight::firstName.name}: $${it::firstName.name}$index,
                ${UserSplitRight::id.name}: $userIdPlaceholder$index,
                ${UserSplitRight::lastActiveAt.name}: $${it::lastActiveAt.name}$index,
                ${UserSplitRight::lastName.name}: $${it::lastName.name}$index
            })
            WITH u$index
            """.trimIndent() to mapOf(
                userIdPlaceholder + index to it.id,
                it::email.name + index to it.email,
                it::firstName.name + index to it.firstName,
                it::lastName.name + index to it.lastName,
                it::lastActiveAt.name + index to it.lastActiveAt.toString()
            )
        }

        val queries = usersCypher + friendsCypher + messagesCypher + groupsCypher +
                membershipsCypher

        if (queries.isEmpty()) {
            return@lazy Query("RETURN 0;")
        }

        val query = "USE `$composite`.`$database`\n" + queries
            .joinToString("\n") { it.first }
            .lines()
            .dropLast(1)
            .joinToString("\n") + ";"

        val params = queries
            .fold(mapOf<String, Any>()) { acc, it -> acc + it.second }
            .mapKeys { it.key.removePrefix("$") }

        Query(
            query,
            params
        )
    }

    override fun invoke(transaction: Transaction) {
        transaction
            .run(query)
            .consume()
    }
}