package route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import service.DatabaseService

object Routes {

    fun Application.configureRouting(databaseService: DatabaseService) {
        routing {
            get {
                call.respondText(
                    this::class.java.classLoader
                        .getResource("./static/index.html")!!
                        .readText(),
                    ContentType.Text.Html
                )
            }

            route("/api/v1/graph") {
                post("/reseed") {
                    try {
                        databaseService.reseed()
                    } catch (e: Exception) {
                        call.respondText(status = HttpStatusCode.InternalServerError) {
                            e.message ?: "Try again later"
                        }
                    }
                }

                get("display") {
                    call.respond(
                        databaseService.getGraph(),
                        typeInfo = typeInfo<List<List<Map<String, Any>>>>()
                    )
                }

                route("/specific") {
                    post("/createFriendship") {
                        databaseService.createRandomFriendship()
                    }

                    post("/createGroup") {
                        databaseService.createRandomGroup()
                    }

                    post("/createPost") {
                        databaseService.createRandomPost()
                    }
                }
            }
        }
    }
}