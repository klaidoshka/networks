package factory

import constant.GroupConstants
import model.Group

object GroupFactory : Factory<Group> {

    override fun create(): Group {
        return Group(
            description = GroupConstants.descriptions.random(),
            name = GroupConstants.names.random()
        )
    }
}