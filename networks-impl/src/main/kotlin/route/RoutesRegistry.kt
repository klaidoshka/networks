package route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.Like
import service.DatabaseService
import service.FriendshipService
import service.GenerationService
import service.LikeService
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class RoutesRegistry(
    private val databaseService: DatabaseService,
    private val friendshipService: FriendshipService,
    private val generationService: GenerationService,
    private val likeService: LikeService
) {

    /**
     * Configures the routing for the application.
     *
     * @param application The application to configure the routing for.
     */
    fun configureRouting(application: Application) = application.run {
        routing {
            staticResources(
                "/",
                "static"
            )

            route("/api/v1") {
                route("/friendship") {
                    configureFriendship()
                }

                route("/graph") {
                    configureGlobal()
                }

                route("/like") {
                    configureLike()
                }
            }
        }
    }

    private fun Route.configureFriendship() {
        post("/create") {
            val params = call.receiveParameters()
            val userId1 = params["userId1"]
            val userId2 = params["userId2"]
            val since = params["since"]

            if (userId1 == null || userId2 == null || since == null) {
                call.respondText(status = HttpStatusCode.BadRequest) {
                    "Missing parameters"
                }

                return@post
            }

            try {
                val result = friendshipService.create(
                    since = DateTimeFormatter
                        .ofPattern("yyyy-MM-dd")
                        .parse(
                            since,
                            LocalDate::from
                        )
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC),
                    userId1 = userId1,
                    userId2 = userId2
                )

                call.respond(
                    result.let {
                        mapOf(
                            "id" to it.id,
                            "since" to it.since.toString()
                        )
                    }
                )
            } catch (e: Exception) {
                call.respondText(status = HttpStatusCode.InternalServerError) {
                    e.message ?: "Failed to create friendship"
                }
            }
        }

        get("/all") {
            try {
                call.respond(friendshipService.getAll())
            } catch (e: Exception) {
                call.respondText(status = HttpStatusCode.InternalServerError) {
                    e.message ?: "Failed to retrieve friendships"
                }
            }
        }

        post("/update") {
            val params = call.receiveParameters()
            val id = params["friendshipId"]
            val since = params["newDate"]

            if (id == null || since == null) {
                call.respondText(status = HttpStatusCode.BadRequest) {
                    "Missing parameters"
                }

                return@post
            }

            try {
                friendshipService.updateDate(
                    id = id,
                    since = DateTimeFormatter
                        .ofPattern("yyyy-MM-dd")
                        .parse(
                            since,
                            LocalDate::from
                        )
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
                )

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respondText(status = HttpStatusCode.InternalServerError) {
                    e.message ?: "Failed to update friendship"
                }
            }
        }

        post("/delete") {
            val params = call.receiveParameters()
            val id = params["friendshipId"]

            if (id == null) {
                call.respondText(status = HttpStatusCode.BadRequest) {
                    "Missing friendshipId"
                }

                return@post
            }

            try {
                friendshipService.delete(id)

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respondText(status = HttpStatusCode.InternalServerError) {
                    e.message ?: "Failed to delete friendship"
                }
            }
        }
    }

    private fun Route.configureGlobal() {
        get("/display") {
            try {
                call.respond(databaseService.getGraph())
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
                generationService.generateNodesInLeftSplit(amount)

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
                generationService.generateNodesInRightSplit(amount)

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
                generationService.generateNodes(amount)

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respondText(status = HttpStatusCode.InternalServerError) {
                    e.message ?: "Try again later"
                }
            }
        }
    }

    private fun Route.configureLike() {
        fun map(like: Like): Map<String, Any> {
            return mapOf(
                "id" to like.id,
                "likedAt" to like.likedAt.toString(),
                "postId" to like.post.id,
                "userId" to like.user.id
            )
        }

        post("/create") {
            val params = call.receiveParameters()
            val userId = params["userId"]
            val postId = params["postId"]

            if (userId == null || postId == null) {
                call.respondText(status = HttpStatusCode.BadRequest) {
                    "Missing parameters"
                }

                return@post
            }

            try {
                val result = likeService.create(
                    userId = userId,
                    postId = postId
                )

                call.respond(result.let(::map))
            } catch (e: Exception) {
                call.respondText(status = HttpStatusCode.InternalServerError) {
                    e.message ?: "Failed to add like onto the post"
                }
            }
        }

        get("/all") {
            try {
                call.respond(
                    likeService
                        .getAll()
                        .map(::map)
                )
            } catch (e: Exception) {
                call.respondText(status = HttpStatusCode.InternalServerError) {
                    e.message ?: "Failed to retrieve liked posts"
                }
            }
        }

        post("/update") {
            val params = call.receiveParameters()
            val id = params["id"]
            val post = params["postId"]

            if (id == null || post == null) {
                call.respondText(status = HttpStatusCode.BadRequest) {
                    "Missing parameters"
                }

                return@post
            }

            try {
                likeService.update(
                    id = id,
                    postId = post
                )

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respondText(status = HttpStatusCode.InternalServerError) {
                    e.message ?: "Failed to update liked post"
                }
            }
        }

        post("/delete") {
            val params = call.receiveParameters()
            val id = params["id"]

            if (id == null) {
                call.respondText(status = HttpStatusCode.BadRequest) {
                    "Missing id"
                }

                return@post
            }

            try {
                likeService.delete(id)

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respondText(status = HttpStatusCode.InternalServerError) {
                    e.message ?: "Failed to delete like"
                }
            }
        }
    }
}