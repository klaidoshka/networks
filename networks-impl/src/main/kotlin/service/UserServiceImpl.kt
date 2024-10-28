package service

import com.google.common.cache.CacheBuilder
import configuration.DbmsInstancesConfiguration
import model.User
import query.UserGetLeftSplitQuery
import query.UserGetRightSplitQuery
import util.DriverUtil.runInParallel
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class UserServiceImpl(
    private val databaseService: DatabaseService,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val userGetLeftQueryFactory: (database: String, id: String) -> UserGetLeftSplitQuery,
    private val userGetRightQueryFactory: (database: String, id: String) -> UserGetRightSplitQuery
) : UserService {

    private val cache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(30.seconds.toJavaDuration())
        .expireAfterAccess(30.seconds.toJavaDuration())
        .build<String, User>()

    override suspend fun getUser(id: String): User? {
        var user = cache.getIfPresent(id)

        if (user != null) {
            return user
        }

        val resultLeft = databaseService.driver
            .runInParallel(
                dbmsInstancesConfiguration.compositeName,
                dbmsInstancesConfiguration.leftSplit.databaseNames.associateWith { database ->
                    { transaction ->
                        userGetLeftQueryFactory(
                            database,
                            id
                        )(transaction)
                    }
                }
            ).values
            .firstNotNullOfOrNull { it }

        if (resultLeft == null) {
            return null
        }

        val resultRight = databaseService.driver
            .runInParallel(
                dbmsInstancesConfiguration.compositeName,
                dbmsInstancesConfiguration.rightSplit.databaseNames.associateWith { database ->
                    { transaction ->
                        userGetRightQueryFactory(
                            database,
                            id
                        )(transaction)
                    }
                }
            ).values
            .firstNotNullOfOrNull { it }

        if (resultRight == null) {
            return null
        }

        user = User(
            birthDate = resultLeft.birthDate,
            email = resultRight.email,
            firstName = resultRight.firstName,
            id = resultLeft.id,
            interests = resultLeft.interests,
            lastActiveAt = resultRight.lastActiveAt,
            lastName = resultRight.lastName,
            location = resultLeft.location,
            registeredAt = resultLeft.registeredAt,
            status = resultLeft.status
        )

        cache.put(
            id,
            user
        )

        return user
    }
}