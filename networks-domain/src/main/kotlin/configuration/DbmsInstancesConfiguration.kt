package configuration

import java.net.URI

/**
 * Configuration of the dbms instance.
 *
 * It is expected that the data is split into two parts:
 * - LeftSplit (User; Posts; Comments; Likes)
 * - RightSplit (User; Groups; Memberships; Friendships; Messages)
 *
 * And these split parts are separate dbms instances.
 *
 * Then instances are holding data that is split into two databases by some criteria:
 * - Primary database (more needed data)
 * - Secondary database (less needed data)
 */
interface DbmsInstancesConfiguration {

    /**
     * Credentials of the dbms instance that contains neo4j composite-db architecture.
     */
    val credentials: Credentials

    /**
     * The name of the composite database.
     */
    val compositeName: String

    /**
     * List of all database names (4):
     * - LeftSplit primary database
     * - LeftSplit secondary database
     * - RightSplit primary database
     * - RightSplit secondary database
     */
    val databaseNames
        get() = listOf(
            leftSplit.primaryDatabaseName,
            leftSplit.secondaryDatabaseName,
            rightSplit.primaryDatabaseName,
            rightSplit.secondaryDatabaseName
        )

    /**
     * Instance of the data left-split.
     */
    val leftSplit: Instance

    /**
     * Instance of the data right-split.
     */
    val rightSplit: Instance

    interface Credentials {

        /**
         * The password of the database.
         */
        val password: String

        /**
         * The URI of the database.
         */
        val uri: URI

        /**
         * The username of the database.
         */
        val username: String
    }

    /**
     * Instance of the dbms.
     */
    interface Instance {

        /**
         * The name of the primary database. More needed data will be stored here.
         */
        val primaryDatabaseName: String

        /**
         * The name of the secondary database. Less needed data will be stored here.
         */
        val secondaryDatabaseName: String
    }

    companion object {

        const val COMPOSITE_NAME = "dbms.compositeName"
        const val CREDENTIALS_PASSWORD = "dbms.credentials.password"
        const val CREDENTIALS_URI = "dbms.credentials.uri"
        const val CREDENTIALS_USERNAME = "dbms.credentials.username"
        const val LEFT_SPLIT_PRIMARY_DB = "dbms.leftSplit.primaryDatabaseName"
        const val LEFT_SPLIT_SECONDARY_DB = "dbms.leftSplit.secondaryDatabaseName"
        const val RIGHT_SPLIT_PRIMARY_DB = "dbms.rightSplit.primaryDatabaseName"
        const val RIGHT_SPLIT_SECONDARY_DB = "dbms.rightSplit.secondaryDatabaseName"
    }
}