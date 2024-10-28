import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import koin.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.context.startKoin
import route.RoutesRegistry
import service.DatabaseService

fun main() {
    val koin = startKoin {
        modules(
            configurationModule,
            factoryModule,
            queryModule,
            routeModule,
            serviceModule
        )
    }.koin

    val scope = CoroutineScope(Dispatchers.Default)

    val server = embeddedServer(
        Netty,
        port = 23567,
        host = "0.0.0.0"
    ) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }

        configure(
            koin = koin,
            scope = scope
        )
    }

    server.addShutdownHook {
        shutdown(
            koin = koin,
            scope = scope
        )
    }

    server.start(wait = true)
}

private fun Application.configure(
    koin: Koin,
    scope: CoroutineScope
) {
    val databaseService by koin.inject<DatabaseService>()

    scope.launch {
        databaseService.start()
    }

    val routesRegistry by koin.inject<RoutesRegistry>()

    routesRegistry.configureRouting(this)
}

private fun shutdown(
    koin: Koin,
    scope: CoroutineScope
) {
    val databaseService by koin.inject<DatabaseService>()

    scope.launch {
        databaseService.stop()
    }

    koin.close()

    scope.cancel()
}