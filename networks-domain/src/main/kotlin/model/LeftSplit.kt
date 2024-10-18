package model

data class LeftSplit(
    val comments: List<Comment>,
    val likes: List<Like>,
    val posts: List<Post>,
    val users: List<UserSplitLeft>
)
