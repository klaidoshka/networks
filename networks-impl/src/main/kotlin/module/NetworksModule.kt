package module

import org.koin.dsl.module
import service.DatabaseService
import service.DatabaseServiceImpl

object NetworksModule {

    val INSTANCE = module {
        single<DatabaseService> { DatabaseServiceImpl() }
    }
}