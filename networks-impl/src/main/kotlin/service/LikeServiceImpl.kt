package service

import configuration.DbmsInstancesConfiguration
import model.Like
import query.LikeCreateQuery
import query.LikeDeleteQuery
import query.LikeGetAllQuery
import query.LikeUpdatePostQuery
import util.DriverUtil.runInParallel

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
        // Creating in all fragments, since cannot know where the post is stored
        // Should make it synchronized and stop on first success or just drop horizontal sharding
        return databaseService.driver.runInParallel(
            dbmsInstancesConfiguration.compositeName,
            dbmsInstancesConfiguration.leftSplit.databaseNames.associateWith { database ->
                { transaction ->
                    likeCreateQueryFactory(
                        database,
                        userId,
                        postId
                    )(transaction)
                }
            }).values
            .firstNotNullOfOrNull { it } ?: throw IllegalStateException("User or post not found")
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
            throw IllegalArgumentException("Like or post not found")
        }
    }
}