package factory

import model.RightSplit
import model.User

/**
 * Creates a [RightSplit] instance from a list of [User] instances.
 */
interface RightSplitFactory {

    /**
     * Creates a [RightSplit] instance.
     *
     * @param users the list of users
     *
     * @return a [RightSplit] instance
     */
    fun create(users: List<User>): RightSplit
}