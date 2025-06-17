package rfm.hillsongptapp.logging

import co.touchlab.kermit.Logger
import kotlin.reflect.KClass


class LoggerHelper {

    companion object {
        private var _tag: String? = null

        fun logDebug(message: String) {
            Logger.d(_tag.orEmpty()) {
                message
            }
        }

        fun logInfo(message: String) {
            Logger.i(_tag.orEmpty()) {
                message
            }
        }

        fun logError(error: Throwable) {
            Logger.e(_tag.orEmpty(), error)
        }
    }
}