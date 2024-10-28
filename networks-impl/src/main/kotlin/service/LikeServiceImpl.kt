package service

import configuration.DbmsInstancesConfiguration
import model.Like
import query.LikeCreateQuery
import query.LikeDeleteQuery
import query.LikeGetAllQuery
import query.LikeUpdatePostQuery
import util.DriverUtil.runInParallel
import util.DriverUtil.runSingle
import java.time.LocalDate

class LikeServiceImpl(
    private val databaseService: DatabaseService,
    private val databaseSplitService: DatabaseSplitService,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val likeCreateQueryFactory: (database: String, userId: String, postId: String) -> LikeCreateQuery,
    private val likeDeleteQueryFactory: (database: String, id: String) -> LikeDeleteQuery,
    private val likeGetQueryFactory: (database: String) -> LikeGetAllQuery,
    private val likeUpdateQueryFactory: (database: String, id: String, postId: String) -> LikeUpdatePostQuery
) : LikeService {

    override suspend fun create(
        userId: String,
        postId: String
    ): Like {
        val primary = databaseSplitService.isInPrimary(
            userId = userId,
            date = LocalDate.now()
        )

        return databaseService.driver.runSingle(dbmsInstancesConfiguration.compositeName) { transaction ->
            likeCreateQueryFactory(
                if (primary) dbmsInstancesConfiguration.leftSplit.primaryDatabaseName
                else dbmsInstancesConfiguration.leftSplit.secondaryDatabaseName,
                userId,
                postId
            )(transaction)
        }
    }

    override suspend fun delete(id: String) {
        // Updating in all fragments, since cannot know where the like is stored
        val result = databaseService.driver
            .runInParallel(
                dbmsInstancesConfiguration.compositeName,
                dbmsInstancesConfiguration.leftSplit.databaseNames.associateWith { database ->
                    { transaction ->
                        likeDeleteQueryFactory(
                            database,
                            id
                        )(transaction)
                    }
                }
            ).values
            .sum()

        if (result == 0) {
            throw IllegalArgumentException("Like with id $id not found")
        }
    }

    override suspend fun getAll(): List<Like> {
        return databaseService.driver
            .runInParallel(
                dbmsInstancesConfiguration.compositeName,
                dbmsInstancesConfiguration.leftSplit.databaseNames.associateWith { database ->
                    { transaction -> likeGetQueryFactory(database)(transaction) }
                }
            ).values
            .flatten()
    }

    override suspend fun update(
        id: String,
        postId: String
    ) {
        // Updating in all fragments, since cannot know where the like is stored
        val result = databaseService.driver
            .runInParallel(
                dbmsInstancesConfiguration.compositeName,
                dbmsInstancesConfiguration.leftSplit.databaseNames.associateWith { database ->
                    { transaction ->
                        likeUpdateQueryFactory(
                            database,
                            id,
                            postId
                        )(transaction)
                    }
                }
            ).values
            .sum()

        if (result == 0) {
            throw IllegalArgumentException("Like with id $id not found")
        }
    }
}