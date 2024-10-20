package route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.GraphDatabaseService

object Routes {

    fun Application.configureRouting(databaseService: GraphDatabaseService) {
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
                get("/display") {
                    call.respond(
                        HttpStatusCode.OK,
                        databaseService.getGraph()
                    )
                }

                post("/delete") {
                    try {
                        databaseService.deleteGraph()

                        call.respond(HttpStatusCode.OK)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError) {
                            e.message ?: "Try again later"
                        }
                    }
                }

                post("/generateLeftSplit") {
                    val amount = call.parameters["amount"]?.toIntOrNull()

                    if (amount == null || amount < 1) {
                        call.respondText(status = HttpStatusCode.BadRequest) {
                            "Invalid or undefined amount"
                        }

                        return@post
                    }

                    try {
                        databaseService.generateNodesInLeftSplit(amount)

                        call.respond(HttpStatusCode.OK)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError) {
                            e.message ?: "Try again later"
                        }
                    }
                }

                post("/generateRightSplit") {
                    val amount = call.parameters["amount"]?.toIntOrNull()

                    if (amount == null || amount < 1) {
                        call.respondText(status = HttpStatusCode.BadRequest) {
                            "Invalid or undefined amount"
                        }

                        return@post
                    }

                    try {
                        databaseService.generateNodesInRightSplit(amount)

                        call.respond(HttpStatusCode.OK)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError) {
                            e.message ?: "Try again later"
                        }
                    }
                }

                post("/generate") {
                    val amount = call.parameters["amount"]?.toIntOrNull()

                    if (amount == null || amount < 1) {
                        call.respondText(status = HttpStatusCode.BadRequest) {
                            "Invalid or undefined amount"
                        }

                        return@post
                    }

                    try {
                        databaseService.generateNodes(amount)

                        call.respond(HttpStatusCode.OK)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError) {
                            e.message ?: "Try again later"
                        }
                    }
                }
            }
        }
    }
}