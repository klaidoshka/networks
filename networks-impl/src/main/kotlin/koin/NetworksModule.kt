package koin

import factory.*
import model.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import service.DatabaseService
import service.DatabaseServiceImpl

object NetworksModule {

    val INSTANCE = module {
        single<Factory<Comment>>(named("commentFactory")) { CommentFactory }
        single<Factory<Friendship>>(named("friendshipFactory")) { FriendshipFactory }
        single<Factory<Group>>(named("groupFactory")) { GroupFactory }
        single<Factory<Like>>(named("likeFactory")) { LikeFactory }
        single<Factory<Membership>>(named("membershipFactory")) { MembershipFactory }
        single<Factory<Message>>(named("messageFactory")) { MessageFactory }
        single<Factory<Post>>(named("postFactory")) { PostFactory }
        single<Factory<User>>(named("userFactory")) { UserFactory }

        single<DatabaseService> {
            DatabaseServiceImpl(
                get(named("leftSplitFactory")),
                get(named("rightSplitFactory")),
                get(named("userFactory"))
            )
        }

        single<LeftSplitFactory>(named("leftSplitFactory")) {
            LeftSplitFactoryImpl(
                get(named("commentFactory")),
                get(named("likeFactory")),
                get(named("postFactory"))
            )
        }

        single<RightSplitFactory>(named("rightSplitFactory")) {
            RightSplitFactoryImpl(
                get(named("commentFactory")),
                get(named("likeFactory")),
                get(named("postFactory")),
                get(named("userFactory"))
            )
        }
    }
}