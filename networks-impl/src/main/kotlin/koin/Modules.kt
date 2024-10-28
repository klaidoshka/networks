package koin

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import configuration.DbmsInstancesConfiguration
import configuration.ExternalDbmsInstancesConfiguration
import factory.*
import model.*
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module
import query.*
import route.RoutesRegistry
import service.*
import java.time.Instant

val configurationModule = module {
    single<Config> { ConfigFactory.load() }
    single<DbmsInstancesConfiguration> { ExternalDbmsInstancesConfiguration(get()) }
}

val factoryModule = module {
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
}

val queryModule = module {
    factory { (database: String) ->
        DeleteGraphQuery(
            database = database,
            dbmsInstancesConfiguration = get()
        )
    }

    factory { (database: String, userId1: String, userId2: String, since: Instant) ->
        FriendshipCreateQuery(
            database = database,
            dbmsInstancesConfiguration = get(),
            userId1 = userId1,
            userId2 = userId2,
            since = since
        )
    }

    factory { (database: String, id: String) ->
        FriendshipDeleteQuery(
            database = database,
            dbmsInstancesConfiguration = get(),
            id = id
        )
    }

    factory { (database: String) ->
        FriendshipGetAllQuery(
            database = database,
            dbmsInstancesConfiguration = get()
        )
    }

    factory { (database: String, id: String, since: Instant) ->
        FriendshipUpdateDateQuery(
            database = database,
            dbmsInstancesConfiguration = get(),
            id = id,
            since = since
        )
    }

    factory { (database: String) ->
        GetGraphQuery(
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

    factory { (database: String, userId: String, postId: String) ->
        LikeCreateQuery(
            database = database,
            dbmsInstancesConfiguration = get(),
            postId = postId,
            userId = userId
        )
    }

    factory { (database: String, id: String) ->
        LikeDeleteQuery(
            database = database,
            dbmsInstancesConfiguration = get(),
            id = id
        )
    }

    factory { (database: String) ->
        LikeGetAllQuery(
            database = database,
            dbmsInstancesConfiguration = get()
        )
    }

    factory { (database: String, id: String, postId: String) ->
        LikeUpdatePostQuery(
            database = database,
            dbmsInstancesConfiguration = get(),
            id = id,
            postId = postId
        )
    }

    factory { (database: String, id: String) ->
        UserGetLeftSplitQuery(
            database = database,
            dbmsInstancesConfiguration = get(),
            id = id
        )
    }

    factory { (database: String, id: String) ->
        UserGetRightSplitQuery(
            database = database,
            dbmsInstancesConfiguration = get(),
            id = id
        )
    }
}

val routeModule = module {
    single<RoutesRegistry> {
        RoutesRegistry(
            databaseService = get(),
            friendshipService = get(),
            generationService = get(),
            likeService = get()
        )
    }
}

val serviceModule = module {
    single<DatabaseService> {
        DatabaseServiceImpl(
            dbmsInstancesConfiguration = get(),
            deleteGraphQueryFactory = { database ->
                get<DeleteGraphQuery> { parametersOf(database) }
            },
            getGraphQueryFactory = { database -> get<GetGraphQuery> { parametersOf(database) } },
            userFactory = get(qualifier<UserFactory>())
        )
    }

    single<DatabaseSplitService> {
        DatabaseSplitServiceImpl(userService = get())
    }

    single<FriendshipService> {
        FriendshipServiceImpl(
            databaseService = get(),
            databaseSplitService = get(),
            dbmsInstancesConfiguration = get(),
            friendshipCreateQueryFactory = { database, userId1, userId2, since ->
                get<FriendshipCreateQuery> {
                    parametersOf(
                        database,
                        userId1,
                        userId2,
                        since
                    )
                }
            },
            friendshipDeleteQueryFactory = { database, id ->
                get<FriendshipDeleteQuery> {
                    parametersOf(
                        database,
                        id
                    )
                }
            },
            friendshipGetQueryFactory = { database ->
                get<FriendshipGetAllQuery> { parametersOf(database) }
            },
            friendshipUpdateQueryFactory = { database, id, since ->
                get<FriendshipUpdateDateQuery> {
                    parametersOf(
                        database,
                        id,
                        since
                    )
                }
            }
        )
    }

    single<GenerationService> {
        GenerationServiceImpl(
            databaseService = get(),
            databaseSplitService = get(),
            dbmsInstancesConfiguration = get(),
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
            rightSplitFactory = get(),
            userFactory = get(qualifier<UserFactory>())
        )
    }

    single<LikeService> {
        LikeServiceImpl(
            databaseService = get(),
            databaseSplitService = get(),
            dbmsInstancesConfiguration = get(),
            likeCreateQueryFactory = { database, userId, postId ->
                get<LikeCreateQuery> {
                    parametersOf(
                        database,
                        userId,
                        postId
                    )
                }
            },
            likeDeleteQueryFactory = { database, id ->
                get<LikeDeleteQuery> {
                    parametersOf(
                        database,
                        id
                    )
                }
            },
            likeGetQueryFactory = { database ->
                get<LikeGetAllQuery> { parametersOf(database) }
            },
            likeUpdateQueryFactory = { database, id, postId ->
                get<LikeUpdatePostQuery> {
                    parametersOf(
                        database,
                        id,
                        postId
                    )
                }
            }
        )
    }

    single<UserService> {
        UserServiceImpl(
            databaseService = get(),
            dbmsInstancesConfiguration = get(),
            userGetLeftQueryFactory = { database, id ->
                get<UserGetLeftSplitQuery> {
                    parametersOf(
                        database,
                        id
                    )
                }
            },
            userGetRightQueryFactory = { database, id ->
                get<UserGetRightSplitQuery> {
                    parametersOf(
                        database,
                        id
                    )
                }
            }
        )
    }
}