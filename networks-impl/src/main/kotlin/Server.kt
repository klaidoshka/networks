import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import koin.networksModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.context.startKoin
import route.Routes.configureRouting
import service.GraphDatabaseService

fun main() {
    val koin = startKoin { modules(networksModule) }.koin
    val scope = CoroutineScope(Dispatchers.Default)

    val server = embeddedServer(
        Netty,
        port = 23567,
        host = "0.0.0.0"
    ) {
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
    val databaseService by koin.inject<GraphDatabaseService>()

    scope.launch {
        databaseService.start()
    }

    configureRouting(databaseService)
}

private fun shutdown(
    koin: Koin,
    scope: CoroutineScope
) {
    val databaseService by koin.inject<GraphDatabaseService>()

    scope.launch {
        databaseService.stop()
    }

    koin.close()

    scope.cancel()
}