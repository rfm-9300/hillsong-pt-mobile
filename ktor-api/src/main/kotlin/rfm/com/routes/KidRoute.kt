
package rfm.com.routes

import io.ktor.server.routing.*
import rfm.com.data.db.kid.KidRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import rfm.com.data.db.kid.Kid

fun Route.kidRoutes(kidRepository: KidRepository) {
    route("/kids") {

        get("/{kidId}") { 
            val kidId = call.parameters["kidId"]?.toIntOrNull()

            if (kidId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid kid ID")
                return@get
            }

            val kid = kidRepository.getKid(kidId)

            if (kid != null) {
                call.respond(kid)
            } else {
                call.respond(HttpStatusCode.NotFound, "Kid not found")
            }
        }

        get("/family/{familyId}") {
            val familyId = call.parameters["familyId"]?.toIntOrNull()

            if (familyId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid family ID")
                return@get
            }

            val kids = kidRepository.getKidsByFamily(familyId)
            call.respond(kids)
        }

        post {
            val kid = call.receive<Kid>()
            val added = kidRepository.addKid(kid)

            if (added) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to add kid")
            }
        }

        put("/{kidId}") {
            val kidId = call.parameters["kidId"]?.toIntOrNull()
            val kid = call.receive<Kid>()

            if (kidId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid kid ID")
                return@put
            }

            val updated = kidRepository.updateKid(kid.copy(id = kidId))

            if (updated) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to update kid")
            }
        }

        delete("/{kidId}") {
            val kidId = call.parameters["kidId"]?.toIntOrNull()

            if (kidId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid kid ID")
                return@delete
            }

            val deleted = kidRepository.deleteKid(kidId)

            if (deleted) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to delete kid")
            }
        }
    }
}
