package factory

import constant.CommentConstants
import model.Comment

object CommentFactory : Factory<Comment> {

    override fun create(): Comment {
        return Comment(content = CommentConstants.comments.random())
    }
}