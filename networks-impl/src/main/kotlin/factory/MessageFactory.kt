package factory

import constant.MessageConstants
import model.Message

object MessageFactory : Factory<Message> {

    override fun create(): Message {
        return Message(
            content = MessageConstants.contents.random()
        )
    }
}