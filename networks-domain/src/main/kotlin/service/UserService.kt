package service

import model.User

/**
 * User service interface. Handles user related operations.
 */
interface UserService {

    /**
     * Get user by id
     *
     * @param id User id
     *
     * @return User object or null if user not found
     */
    suspend fun getUser(id: String): User?
}