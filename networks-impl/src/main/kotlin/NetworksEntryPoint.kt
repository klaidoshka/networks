import io.ktor.server.engine.*
import io.ktor.server.netty.*
import module.Modules
import org.koin.core.context.startKoin
import route.Routes.configureRouting

fun main() {
    startKoin {
        modules(Modules.Networks)
    }

    val server = embeddedServer(
        Netty,
        port = 5000,
        host = "0.0.0.0",
        module = { configureRouting() }
    )

    server.start(wait = true)
}