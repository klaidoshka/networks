package service

import configuration.DbmsInstancesConfiguration
import model.Friendship
import query.FriendshipCreateQuery
import query.FriendshipDeleteQuery
import query.FriendshipGetAllQuery
import query.FriendshipUpdateDateQuery
import util.DriverUtil.runInParallel
import util.DriverUtil.runSingle
import java.time.Instant
import java.time.ZoneId

class FriendshipServiceImpl(
    private val databaseService: DatabaseService,
    private val databaseSplitService: DatabaseSplitService,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val friendshipCreateQueryFactory: (database: String, userId1: String, userId2: String, since: Instant) -> FriendshipCreateQuery,
    private val friendshipDeleteQueryFactory: (database: String, id: String) -> FriendshipDeleteQuery,
    private val friendshipGetQueryFactory: (database: String) -> FriendshipGetAllQuery,
    private val friendshipUpdateQueryFactory: (database: String, id: String, date: Instant) -> FriendshipUpdateDateQuery
) : FriendshipService {

    override suspend fun create(
        userId1: String,
        userId2: String,
        since: Instant
    ): Friendship {
        val primary = databaseSplitService.isInPrimary(
            userId1 = userId1,
            userId2 = userId2,
            date = since
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        )

        return databaseService.driver.runSingle(dbmsInstancesConfiguration.compositeName) { transaction ->
            friendshipCreateQueryFactory(
                if (primary) dbmsInstancesConfiguration.rightSplit.primaryDatabaseName
                else dbmsInstancesConfiguration.rightSplit.secondaryDatabaseName,
                userId1,
                userId2,
                since
            )(transaction)
        }
    }

    override suspend fun delete(id: String) {
        // Updating in all fragments, since cannot know where the friendship is stored
        databaseService.driver.runInParallel(
            dbmsInstancesConfiguration.compositeName,
            dbmsInstancesConfiguration.rightSplit.databaseNames.associateWith { database ->
                { transaction ->
                    friendshipDeleteQueryFactory(
                        database,
                        id
                    )(transaction)
                }
            }
        )
    }

    override suspend fun getAll(): List<Map<String, Any>> {
        return databaseService.driver
            .runInParallel(
                dbmsInstancesConfiguration.compositeName,
                dbmsInstancesConfiguration.rightSplit.databaseNames.associateWith { database ->
                    { transaction -> friendshipGetQueryFactory(database)(transaction) }
                }
            ).values
            .flatten()
    }

    override suspend fun updateDate(
        id: String,
        since: Instant
    ) {
        // Updating in all fragments, since cannot know where the friendship is stored
        databaseService.driver.runInParallel(
            dbmsInstancesConfiguration.compositeName,
            dbmsInstancesConfiguration.rightSplit.databaseNames.associateWith { database ->
                { transaction ->
                    friendshipUpdateQueryFactory(
                        database,
                        id,
                        since
                    )(transaction)
                }
            }
        )
    }
}