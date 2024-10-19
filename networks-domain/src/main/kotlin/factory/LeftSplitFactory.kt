package factory

import model.LeftSplit
import model.User

/**
 * Creates a [LeftSplit] object from a list of [User] objects.
 */
interface LeftSplitFactory {

    /**
     * Creates a [LeftSplit] object from a list of [User] objects.
     *
     * @param users The list of [User] objects.
     *
     * @return The [LeftSplit] object.
     */
    fun create(users: List<User>): LeftSplit
}