package example.com.routes

import example.com.data.db.event.Event
import example.com.data.db.event.EventRepository
import example.com.data.db.user.UserRepository
import example.com.data.requests.ApproveUserEventRequest
import example.com.data.requests.EventRequest
import example.com.data.requests.RemoveUserEventRequest
import example.com.data.responses.CreateEventResponse
import example.com.data.utils.SseAction
import example.com.data.utils.SseManager
import example.com.plugins.Logger
import example.com.web.pages.homePage.eventTab.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.html.body
import kotlinx.io.readByteArray
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Route.eventRoutes(
    eventRepository: EventRepository,
    sseManager: SseManager,
    userRepository: UserRepository
) {
    /**
     * API Routes
     */

    // api approve user
    authenticate {
        post(Routes.Api.Event.APPROVE_USER) {
            try {
                val request = call.receive<ApproveUserEventRequest>()
                val requestUserId = getUserIdFromRequestToken(call) ?: return@post respondHelper(success = false, message = "User not found", call = call)

                if(!isUserAdmin(requestUserId)) {
                    return@post respondHelper(success = false, message = "Unauthorized", call = call)
                }

                val event = eventRepository.getEvent(request.eventId) ?: return@post respondHelper(success = false, message = "Event not found", call = call)

                val user = userRepository.getUserById(requestUserId.toInt()) ?: return@post respondHelper(success = false, message = "User not found", call = call)

                if(user.id == null) return@post respondHelper(success = false, message = "User not found", call = call)

                val approved = eventRepository.approveUser(eventId = request.eventId, userId = user.id)

                call.respond(HttpStatusCode.OK, CreateEventResponse(
                    success = approved,
                    message = if (approved) "User approved" else "Failed to approve user"
                ))
            } catch (e: Exception) {
                Logger.d("Error approving user: ${e.message}")
                call.respond(
                    HttpStatusCode.BadRequest, CreateEventResponse(
                        success = false,
                        message = "Error approving user: ${e.message}"
                    )
                )
            }
        }
    }

    // api remove user from event
    authenticate {
        post(Routes.Api.Event.REMOVE_USER) {
            try {
                val request = call.receive<RemoveUserEventRequest>()
                val requestUserId = getUserIdFromRequestToken(call) ?: return@post respondHelper(success = false, message = "User not found", call = call)

                if(!isUserAdmin(requestUserId)) {
                    return@post respondHelper(success = false, message = "Unauthorized", call = call)
                }

                val event = eventRepository.getEvent(request.eventId) ?: return@post respondHelper(success = false, message = "Event not found", call = call)

                // Make sure the requester is either an admin or the event organizer
                if(requestUserId.toInt() != event.organizerId && !isUserAdmin(requestUserId)) {
                    return@post respondHelper(success = false, message = "Unauthorized", call = call)
                }

                val removed = eventRepository.removeUserFromEvent(eventId = request.eventId, userId = request.userId)
                
                if (removed) {
                    sseManager.emitEvent(SseAction.RefreshEvents)
                }

                call.respond(HttpStatusCode.OK, CreateEventResponse(
                    success = removed,
                    message = if (removed) "User removed successfully" else "Failed to remove user"
                ))
            } catch (e: Exception) {
                Logger.d("Error removing user: ${e.message}")
                call.respond(
                    HttpStatusCode.BadRequest, CreateEventResponse(
                        success = false,
                        message = "Error removing user: ${e.message}"
                    )
                )
            }
        }
    }

    //api update event
    authenticate {
        post(Routes.Api.Event.UPDATE) {
            try {
                val multiPart = call.receiveMultipart()
                var eventId = 0
                var title = ""
                var description = ""
                var date = ""
                var location = ""
                var image = ""
                var maxAttendees = 0

                multiPart.forEachPart {
                    when(it) {
                        is PartData.FormItem -> {
                            when(it.name) {
                                "eventId" -> eventId = it.value.toInt()
                                "title" -> title = it.value
                                "description" -> description = it.value
                                "date" -> date = it.value
                                "location" -> location = it.value
                                "maxAttendees" -> maxAttendees = it.value.toInt()
                            }
                        }
                        is PartData.FileItem -> {
                            if (it.name == "image") {
                                val fileName = it.originalFileName ?: "unnamed.jpg"
                                val fileBytes = it.provider().readRemaining().readByteArray()
                                image = ImageFileHandler.saveImage(fileBytes, fileName)
                            }
                        }
                        else -> {}
                    }
                    it.dispose
                }

                val event = Event(
                    id = eventId,
                    title = title,
                    description = description,
                    date = LocalDateTime.now(),
                    location = location,
                    organizerId = 1,
                    headerImagePath = image,
                    maxAttendees = maxAttendees
                )
                Logger.d("Updating event: $event")

                if (!eventRepository.updateEvent(event)) throw Exception("Error updating event")
                Logger.d("Event updated successfully")

                call.respond(
                    HttpStatusCode.OK,
                    CreateEventResponse(
                        success = true,
                        message = "Event updated successfully"
                    )
                )
            } catch (e: Exception) {
                Logger.d("Error updating event: ${e.message}")
                call.respond(
                    HttpStatusCode.BadRequest, CreateEventResponse(
                        success = false,
                        message = "Error updating event: ${e.message}"
                    )
                )
            }
        }
    }

    //api delete event
    authenticate {
        post(Routes.Api.Event.DELETE) {
            try {
                val request = kotlin.runCatching { call.receiveNullable<EventRequest>() }.getOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
                val deletedEventAttendees = eventRepository.deleteEventAttendees(request.eventId)

                val deletedEvent = eventRepository.deleteEvent(request.eventId)

                if (deletedEvent) {
                    sseManager.emitEvent(SseAction.RefreshEvents)
                }
                call.respond (HttpStatusCode.OK,
                    if (deletedEvent) CreateEventResponse.success() else CreateEventResponse.failure()
                )
            } catch (e: Exception) {
                Logger.d("Error deleting event: ${e.message}")
                call.respond(
                    HttpStatusCode.BadRequest, CreateEventResponse(
                        success = false,
                        message = "Error deleting event: ${e.message}"
                    )
                )
            }
        }
    }

    //api create event
    authenticate {
        post(Routes.Api.Event.CREATE) {
            val userId = getUserIdFromRequestToken(call) ?: return@post respondHelper(success = false, message = "User not found", call = call)
            try {
                val multiPart = call.receiveMultipart()
                var source = ""
                var title = ""
                var description = ""
                var date = ""
                var location = ""
                var image = ""
                var maxAttendees = 0
                var needsApproval = false

                multiPart.forEachPart {
                    when(it) {
                        is PartData.FormItem -> {
                            when(it.name) {
                                "source" -> source = it.value
                                "title" -> title = it.value
                                "description" -> description = it.value
                                "date" -> date = it.value
                                "location" -> location = it.value
                                "maxAttendees" -> maxAttendees = it.value.toInt()
                                "needsApproval" -> needsApproval = it.value.toBoolean()
                            }
                        }
                        is PartData.FileItem -> {
                            if (it.name == "image") {
                                val fileName = it.originalFileName ?: "unnamed.jpg"
                                val fileBytes = it.provider().readRemaining().readByteArray()
                                image = ImageFileHandler.saveImage(fileBytes, fileName)
                            }
                        }
                        else -> {}
                    }
                    it.dispose
                }

                // Parse the date string to LocalDateTime
                val parsedDate = try {
                    LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (e: Exception) {
                    Logger.d("Error parsing date: $date")
                    throw Exception("Invalid date format")
                }

                val event = Event(
                    title = title,
                    description = description,
                    date = parsedDate,
                    location = location,
                    organizerId = userId.toInt(),
                    headerImagePath = image,
                    maxAttendees = maxAttendees,
                    needsApproval = needsApproval
                )

                Logger.d("Attempting to create event: $event")
                val eventID = eventRepository.addEvent(event)
                if (eventID == null) {
                    throw Exception("Failed to add event to database")
                }
                Logger.d("Event created successfully")
                call.respond(
                    HttpStatusCode.Created,
                    CreateEventResponse(
                        success = true,
                        message = "Event created successfully"
                    )
                )
            } catch (e: Exception) {
                Logger.d("Error creating event: ${e.message}")
                call.respond(
                    HttpStatusCode.BadRequest, CreateEventResponse(
                        success = false,
                        message = "Error creating event: ${e.message}"
                    )
                )
            }
        }
    }

    // api join event
    authenticate {
        post(Routes.Api.Event.JOIN_EVENT) {
            val principal = call.principal<JWTPrincipal>() ?: return@post respondHelper(success = false, message = "User not found", call = call)
            val userId = principal.getClaim("userId", String::class)
            Logger.d("User ID: $userId")

            // Add explicit null check and empty string check
            if (userId.isNullOrEmpty() || userId == "null") {
                return@post respondHelper(success = false, message = "User not found", call = call)
            }

            val eventId = call.receive<EventRequest>().eventId
            val attendees = eventRepository.getEventAttendees(eventId)
            Logger.d("Attendees: $attendees")

            val event = eventRepository.getEvent(eventId) ?: return@post respondHelper(success = false, message = "Event not found", call = call)

            // check if event is full
            if (event.attendees.size >= event.maxAttendees) {
                return@post respondHelper(success = false, message = "Event is full", call = call)
            }

            // check if user is already in the event
            event.attendees.forEach {
                if (it.id == userId.toInt()) {
                    return@post respondHelper(success = false, message = "User already in event", call = call)
                }
            }

            Logger.d("user $userId joining event $eventId")

            if (event.needsApproval) {
                event.waitingList.forEach { if (it.user.userId == userId.toInt()) {
                        return@post respondHelper(success = false, message = "User already in waiting list", call = call)
                    }
                }
                val joined = eventRepository.joinEventWaitingList(eventId, userId.toInt())

                call.respond(HttpStatusCode.OK, CreateEventResponse(
                    success = joined,
                    message = if (joined) "Joined waiting list. Wait until the organizer approves you" else "Failed to join event"
                ))
                return@post
            }

            val joined = eventRepository.joinEvent(eventId, userId.toInt())
            if (joined) {
                sseManager.emitEvent(SseAction.RefreshEvents)
            }
            call.respond(HttpStatusCode.OK, CreateEventResponse(
                success = joined,
                message = if (joined) "Joined event" else "Failed to join event"
            ))
        }
    }


    /**
     * UI Routes
     */

    //ui update event
    get(Routes.Ui.Event.UPDATE) {
        val eventId = call.parameters["eventId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
        val event = eventRepository.getEvent(eventId) ?: return@get call.respond(HttpStatusCode.NotFound)
        call.respondHtml(HttpStatusCode.OK){
            updateEvent(event)
        }
    }

    // event details
    get(Routes.Ui.Event.DETAILS) {
        val eventId = call.parameters["eventId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
        val event = eventRepository.getEvent(eventId) ?: return@get call.respond(HttpStatusCode.NotFound)
        val user = getUserIdFromCookies()?.let { userRepository.getUserProfile(it) }
        call.respondHtml(HttpStatusCode.OK){
            eventDetail(event, user)
        }
    }

    // create event
    get(Routes.Ui.Event.CREATE) {
        call.respondHtml(HttpStatusCode.OK){
            createEvent()
        }
    }

    get(Routes.Ui.Event.LIST_UPCOMING) {
        val userId = getUserIdFromCookies()
        call.respondHtml(HttpStatusCode.OK){
            body {
                upcomingEvents(
                    eventRepository = eventRepository,
                    isAdminRequest = isUserAdmin(getUserIdFromCookies().toString())
                )
            }
        }
    }

    get(Routes.Ui.Event.LIST_PAST) {
        val userId = getUserIdFromCookies()
        call.respondHtml(HttpStatusCode.OK){
            body {
                pastEvents(
                    eventRepository = eventRepository,
                    isAdminRequest = isUserAdmin(getUserIdFromCookies().toString())
                )
            }
        }
    }

}