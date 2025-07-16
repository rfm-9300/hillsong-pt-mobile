
package rfm.com.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import rfm.com.data.db.checkin.CheckIn
import rfm.com.data.db.checkin.CheckInRepository

fun Route.checkInRoutes(checkInRepository: CheckInRepository) {
    route("/checkin") {
        get("/{checkInId}") {
            val checkInId = call.parameters["checkInId"]?.toIntOrNull()

            if (checkInId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid check-in ID")
                return@get
            }

            val checkIn = checkInRepository.getCheckIn(checkInId)

            if (checkIn != null) {
                call.respond(checkIn)
            } else {
                call.respond(HttpStatusCode.NotFound, "Check-in not found")
            }
        }

        get("/kid/{kidId}") {
            val kidId = call.parameters["kidId"]?.toIntOrNull()

            if (kidId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid kid ID")
                return@get
            }

            val checkIns = checkInRepository.getCheckInsByKid(kidId)
            call.respond(checkIns)
        }

        get("/service/{serviceId}") {
            val serviceId = call.parameters["serviceId"]?.toIntOrNull()

            if (serviceId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid service ID")
                return@get
            }

            val checkIns = checkInRepository.getCheckInsByService(serviceId)
            call.respond(checkIns)
        }

        post {
            val checkIn = call.receive<CheckIn>()
            val added = checkInRepository.addCheckIn(checkIn)

            if (added) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to add check-in")
            }
        }

        put("/{checkInId}") {
            val checkInId = call.parameters["checkInId"]?.toIntOrNull()
            val checkIn = call.receive<CheckIn>()

            if (checkInId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid check-in ID")
                return@put
            }

            val updated = checkInRepository.updateCheckIn(checkIn.copy(id = checkInId))

            if (updated) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to update check-in")
            }
        }
    }
}
