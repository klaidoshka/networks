package koin

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import configuration.DbmsInstancesConfiguration
import configuration.ExternalDbmsInstancesConfiguration
import factory.*
import model.*
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module
import query.DetachDeleteAllQuery
import query.InsertLeftSplitQuery
import query.InsertRightSplitQuery
import query.MatchAllQuery
import route.RoutesRegistry
import service.GraphDatabaseService
import service.GraphDatabaseServiceImpl

/**
 * The module containing the definitions for the network components.
 */
val networksModule = module {
    // Configuration
    single<Config> { ConfigFactory.load() }
    single<DbmsInstancesConfiguration> { ExternalDbmsInstancesConfiguration(get()) }

    // Factory
    single<Factory<Comment>>(qualifier<CommentFactory>()) { CommentFactory }
    single<Factory<Friendship>>(qualifier<FriendshipFactory>()) { FriendshipFactory }
    single<Factory<Group>>(qualifier<GroupFactory>()) { GroupFactory }
    single<Factory<Like>>(qualifier<LikeFactory>()) { LikeFactory }
    single<Factory<Membership>>(qualifier<MembershipFactory>()) { MembershipFactory }
    single<Factory<Message>>(qualifier<MessageFactory>()) { MessageFactory }
    single<Factory<Post>>(qualifier<PostFactory>()) { PostFactory }
    single<Factory<User>>(qualifier<UserFactory>()) { UserFactory }

    single<LeftSplitFactory> {
        LeftSplitFactoryImpl(
            commentFactory = get(qualifier<CommentFactory>()),
            likeFactory = get(qualifier<LikeFactory>()),
            postFactory = get(qualifier<PostFactory>())
        )
    }

    single<RightSplitFactory> {
        RightSplitFactoryImpl(
            friendshipFactory = get(qualifier<FriendshipFactory>()),
            groupFactory = get(qualifier<GroupFactory>()),
            membershipFactory = get(qualifier<MembershipFactory>()),
            messageFactory = get(qualifier<MessageFactory>())
        )
    }

    // Moshi
    single<Moshi> {
        Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
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

    // Ktor
    single<RoutesRegistry> {
        RoutesRegistry(
            databaseService = get(),
            moshi = get()
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
            userFactory = get(qualifier<UserFactory>())
        )
    }
}