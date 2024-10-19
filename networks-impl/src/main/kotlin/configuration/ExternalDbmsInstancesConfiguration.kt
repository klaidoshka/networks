package configuration

import com.typesafe.config.Config
import java.net.URI

class ExternalDbmsInstancesConfiguration(config: Config) : DbmsInstancesConfiguration {

    override val credentials = Credentials(
        password = config.getString(DbmsInstancesConfiguration.CREDENTIALS_PASSWORD),
        uri = URI(config.getString(DbmsInstancesConfiguration.CREDENTIALS_URI)),
        username = config.getString(DbmsInstancesConfiguration.CREDENTIALS_USERNAME)
    )
    override val compositeName: String = config.getString(DbmsInstancesConfiguration.COMPOSITE_NAME)

    override val leftSplit = Instance(
        primaryDatabaseName = config.getString(DbmsInstancesConfiguration.LEFT_SPLIT_PRIMARY_DB),
        secondaryDatabaseName = config.getString(DbmsInstancesConfiguration.LEFT_SPLIT_SECONDARY_DB)
    )

    override val rightSplit = Instance(
        primaryDatabaseName = config.getString(DbmsInstancesConfiguration.RIGHT_SPLIT_PRIMARY_DB),
        secondaryDatabaseName = config.getString(DbmsInstancesConfiguration.RIGHT_SPLIT_SECONDARY_DB)
    )

    data class Credentials(
        override val password: String,
        override val uri: URI,
        override val username: String
    ) : DbmsInstancesConfiguration.Credentials

    data class Instance(
        override val primaryDatabaseName: String,
        override val secondaryDatabaseName: String
    ) : DbmsInstancesConfiguration.Instance
}