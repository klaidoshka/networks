package route

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object Routes {

    fun Application.configureRouting() {
        routing {
            route("/api/v1/") {
                configureLeftGraph()

                configureRightGraph()
            }
        }
    }

    private fun Route.configureLeftGraph() {
        route("graph/left-1/") {
            get("") {
                call.respondText("Hello World! (L)")
            }
        }
    }

    private fun Route.configureRightGraph() {
        route("graph/right-1/") {
            get("") {
                call.respondText("Hello World! (R)")
            }
        }
    }
}