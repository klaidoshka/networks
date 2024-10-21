package route

import com.squareup.moshi.Moshi
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.commons.lang3.reflect.TypeLiteral
import service.GraphDatabaseService

class RoutesRegistry(
    private val databaseService: GraphDatabaseService,
    private val moshi: Moshi
) {

    /**
     * Configures the routing for the application.
     *
     * @param application The application to configure the routing for.
     */
    fun configureRouting(application: Application) = application.run {
        val mapJsonAdapter = moshi.adapter<Map<String, Any>>(
            object : TypeLiteral<Map<String, Any>>() {}.type
        )

        routing {
            get {
                call.respondText(
                    contentType = ContentType.Text.Html,
                    text = this::class.java.classLoader
                        .getResource("./static/index.html")!!
                        .readText()
                )
            }

            route("/api/v1/graph") {
                get("/display") {
                    try {
                        call.respondText(
                            contentType = ContentType.Application.Json,
                            text = mapJsonAdapter.toJson(databaseService.getGraph())
                        )
                    } catch (e: Exception) {
                        call.respondText(status = HttpStatusCode.InternalServerError) {
                            e.message ?: "Try again later"
                        }
                    }
                }

                post("/delete") {
                    try {
                        databaseService.deleteGraph()

                        call.respond(HttpStatusCode.OK)
                    } catch (e: Exception) {
                        call.respondText(status = HttpStatusCode.InternalServerError) {
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
                        call.respondText(status = HttpStatusCode.InternalServerError) {
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
                        call.respondText(status = HttpStatusCode.InternalServerError) {
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
                        call.respondText(status = HttpStatusCode.InternalServerError) {
                            e.message ?: "Try again later"
                        }
                    }
                }
            }
        }
    }
}