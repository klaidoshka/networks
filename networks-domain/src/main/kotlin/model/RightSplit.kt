package model

data class RightSplit(
    val friendships: List<Friendship>,
    val groups: List<Group>,
    val memberships: List<Membership>,
    val messages: List<Message>,
    val users: List<UserSplitRight>
)