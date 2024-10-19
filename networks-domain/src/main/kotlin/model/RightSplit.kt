package model

/**
 * Container for the right split of the data.
 *
 * @property friendships The friendships in the right split.
 * @property groups The groups in the right split.
 * @property memberships The memberships in the right split.
 * @property messages The messages in the right split.
 * @property users The users in the right split.
 */
data class RightSplit(
    val friendships: List<Friendship>,
    val groups: List<Group>,
    val memberships: List<Membership>,
    val messages: List<Message>,
    val users: List<UserSplitRight>
)