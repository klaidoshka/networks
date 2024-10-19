package model

/**
 * Container for the left split of the data.
 * 
 * @property comments The comments in the left split.
 * @property likes The likes in the left split.
 * @property posts The posts in the left split.
 * @property users The users in the left split.
 */
data class LeftSplit(
    val comments: List<Comment>,
    val likes: List<Like>,
    val posts: List<Post>,
    val users: List<UserSplitLeft>
)