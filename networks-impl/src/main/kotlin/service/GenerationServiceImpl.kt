package service

import configuration.DbmsInstancesConfiguration
import factory.Factory
import factory.LeftSplitFactory
import factory.RightSplitFactory
import model.LeftSplit
import model.RightSplit
import model.User
import query.InsertLeftSplitQuery
import query.InsertRightSplitQuery
import util.DriverUtil.runInParallel

class GenerationServiceImpl(
    private val databaseService: DatabaseService,
    private val databaseSplitService: DatabaseSplitService,
    private val dbmsInstancesConfiguration: DbmsInstancesConfiguration,
    private val insertLeftSplitQueryFactory: (LeftSplit, toPrimary: Boolean) -> InsertLeftSplitQuery,
    private val insertRightSplitQueryFactory: (RightSplit, toPrimary: Boolean) -> InsertRightSplitQuery,
    private val leftSplitFactory: LeftSplitFactory,
    private val rightSplitFactory: RightSplitFactory,
    private val userFactory: Factory<User>
) : GenerationService {

    override suspend fun generateNodes(amount: Int) {
        val users = userFactory.create(amount)
        val (leftSplit1, leftSplit2) = databaseSplitService.split(leftSplitFactory.create(users))
        val (rightSplit1, rightSplit2) = databaseSplitService.split(rightSplitFactory.create(users))

        databaseService.driver.runInParallel(
            dbmsInstancesConfiguration.compositeName,
            listOf(
                leftSplit1,
                leftSplit2,
                rightSplit1,
                rightSplit2
            )
                .associateWith { split ->
                    { transaction ->
                        when (split) {
                            is LeftSplit -> insertLeftSplitQueryFactory(
                                split,
                                split == leftSplit1
                            )(transaction)

                            is RightSplit -> insertRightSplitQueryFactory(
                                split,
                                split == rightSplit1
                            )(transaction)
                        }
                    }
                }
        )
    }

    override suspend fun generateNodesInLeftSplit(amount: Int) {
        val users = userFactory.create(amount)
        val (leftSplit1, leftSplit2) = databaseSplitService.split(leftSplitFactory.create(users))

        databaseService.driver.runInParallel(
            dbmsInstancesConfiguration.compositeName,
            listOf(
                leftSplit1,
                leftSplit2
            )
                .associateWith { split ->
                    { transaction ->
                        insertLeftSplitQueryFactory(
                            split,
                            split == leftSplit1
                        )(transaction)
                    }
                }
        )
    }

    override suspend fun generateNodesInRightSplit(amount: Int) {
        val users = userFactory.create(amount)
        val (rightSplit1, rightSplit2) = databaseSplitService.split(rightSplitFactory.create(users))

        databaseService.driver.runInParallel(
            dbmsInstancesConfiguration.compositeName,
            listOf(
                rightSplit1,
                rightSplit2
            )
                .associateWith { split ->
                    { transaction ->
                        insertRightSplitQueryFactory(
                            split,
                            split == rightSplit1
                        )(transaction)
                    }
                }
        )
    }
}