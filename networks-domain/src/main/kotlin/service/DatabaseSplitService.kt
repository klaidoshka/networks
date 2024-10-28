package service

import model.*
import java.time.LocalDate

/**
 * Database split service.
 *
 * It is responsible for splitting the graph data horizontally, finding out the criteria for the split
 * and where to split the data.
 */
interface DatabaseSplitService {

    /**
     * Check if the associated to the date data is in primary horizontal split. If not, it is in
     * secondary split.
     *
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @param date Date to check
     *
     * @return True if the split is in primary, false otherwise - is in secondary split.
     */
    suspend fun isInPrimary(
        userId1: String,
        userId2: String,
        date: LocalDate
    ): Boolean

    /**
     * Check if the associated to the date data is in primary horizontal split. If not, it is in
     * secondary split.
     *
     * @param userId User ID
     * @param date Date to check
     *
     * @return True if the split is in primary, false otherwise - is in secondary split.
     */
    suspend fun isInPrimary(
        userId: String,
        date: LocalDate
    ): Boolean

    /**
     * Check if the associated to the date data is in primary horizontal split. If not, it is in
     * secondary split.
     *
     * @param user1 First user
     * @param user2 Second user
     * @param date Date to check
     *
     * @return True if the split is in primary, false otherwise - is in secondary split.
     */
    fun isInPrimary(
        user1: UserSplitLeft,
        user2: UserSplitLeft,
        date: LocalDate
    ): Boolean

    /**
     * Check if the associated to the date data is in primary horizontal split. If not, it is in
     * secondary split.
     *
     * @param user User to check
     * @param date Date to check
     *
     * @return True if the split is in primary, false otherwise - is in secondary split.
     */
    fun isInPrimary(
        user: UserSplitLeft,
        date: LocalDate
    ): Boolean

    /**
     * Check if the associated to the date data is in primary horizontal split. If not, it is in
     * secondary split.
     *
     * @param user1 First user
     * @param user2 Second user
     * @param date Date to check
     *
     * @return True if the split is in primary, false otherwise - is in secondary split.
     */
    fun isInPrimary(
        user1: UserSplitRight,
        user2: UserSplitRight,
        date: LocalDate
    ): Boolean

    /**
     * Check if the associated to the date data is in primary horizontal split. If not, it is in
     * secondary split.
     *
     * @param user User to check
     * @param date Date to check
     *
     * @return True if the split is in primary, false otherwise - is in secondary split.
     */
    fun isInPrimary(
        user: UserSplitRight,
        date: LocalDate
    ): Boolean

    /**
     * Split the graph data horizontally
     *
     * @param leftSplit Left split data in graph to split
     *
     * @return Pair of left splits, divided horizontally by some criteria
     */
    suspend fun split(leftSplit: LeftSplit): Pair<LeftSplit, LeftSplit>

    /**
     * Split the graph data horizontally
     *
     * @param rightSplit Right split data in graph to split
     *
     * @return Pair of right splits, divided horizontally by some criteria
     */
    suspend fun split(rightSplit: RightSplit): Pair<RightSplit, RightSplit>
}