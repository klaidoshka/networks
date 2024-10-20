package koin

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import configuration.DbmsInstancesConfiguration
import configuration.ExternalDbmsInstancesConfiguration
import factory.*
import model.*
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import query.DetachDeleteAllQuery
import query.InsertLeftSplitQuery
import query.InsertRightSplitQuery
import query.MatchAllQuery
import service.GraphDatabaseService
import service.GraphDatabaseServiceImpl

val networksModule = module {
    // Configuration
    single<Config> { ConfigFactory.load() }
    single<DbmsInstancesConfiguration> { ExternalDbmsInstancesConfiguration(get()) }

    // Factory
    single<Factory<Comment>>(named("commentFactory")) { CommentFactory }
    single<Factory<Friendship>>(named("friendshipFactory")) { FriendshipFactory }
    single<Factory<Group>>(named("groupFactory")) { GroupFactory }
    single<Factory<Like>>(named("likeFactory")) { LikeFactory }
    single<Factory<Membership>>(named("membershipFactory")) { MembershipFactory }
    single<Factory<Message>>(named("messageFactory")) { MessageFactory }
    single<Factory<Post>>(named("postFactory")) { PostFactory }
    single<Factory<User>>(named("userFactory")) { UserFactory }

    single<LeftSplitFactory> {
        LeftSplitFactoryImpl(
            get(named("commentFactory")),
            get(named("likeFactory")),
            get(named("postFactory"))
        )
    }

    single<RightSplitFactory> {
        RightSplitFactoryImpl(
            get(named("commentFactory")),
            get(named("likeFactory")),
            get(named("postFactory")),
            get(named("userFactory"))
        )
    }

    // Query
    factory { (database: String) ->
        DetachDeleteAllQuery(
            database = database,
            dbmsInstancesConfiguration = get()
        )
    }

    factory { (leftSplit: LeftSplit, toPrimary: Boolean) ->
        InsertLeftSplitQuery(
            dbmsInstancesConfiguration = get(),
            leftSplit = leftSplit,
            toPrimary = toPrimary
        )
    }

    factory { (rightSplit: RightSplit, toPrimary: Boolean) ->
        InsertRightSplitQuery(
            dbmsInstancesConfiguration = get(),
            rightSplit = rightSplit,
            toPrimary = toPrimary
        )
    }

    factory { (database: String) ->
        MatchAllQuery(
            database = database,
            dbmsInstancesConfiguration = get()
        )
    }

    // Service
    single<GraphDatabaseService> {
        GraphDatabaseServiceImpl(
            dbmsInstancesConfiguration = get(),
            detachDeleteAllQueryFactory = { database ->
                get<DetachDeleteAllQuery> { parametersOf(database) }
            },
            insertLeftSplitQueryFactory = { leftSplit, toPrimary ->
                get<InsertLeftSplitQuery> {
                    parametersOf(
                        leftSplit,
                        toPrimary
                    )
                }
            },
            insertRightSplitQueryFactory = { rightSplit, toPrimary ->
                get<InsertRightSplitQuery> {
                    parametersOf(
                        rightSplit,
                        toPrimary
                    )
                }
            },
            leftSplitFactory = get(),
            matchAllQueryFactory = { database -> get<MatchAllQuery> { parametersOf(database) } },
            rightSplitFactory = get(),
            userFactory = get(named("userFactory"))
        )
    }
}