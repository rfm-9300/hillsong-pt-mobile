package rfm.hillsongptapp.logging

import co.touchlab.kermit.Logger
import kotlin.reflect.KClass

class LoggerHelper {
    companion object {
        private const val TAG_PREFIX = "TIMBER"
        private var _tag: String = TAG_PREFIX

        fun setTag(tag: String) {
            _tag = "$TAG_PREFIX:$tag"
        }

        fun logDebug(message: String, tag: String? = null) {
            val finalTag = tag?.let { "$TAG_PREFIX:$it" } ?: _tag
            Logger.d(finalTag) {
                message
            }
        }

        fun logInfo(message: String, tag: String? = null) {
            val finalTag = tag?.let { "$TAG_PREFIX:$it" } ?: _tag
            Logger.i(finalTag) {
                message
            }
        }

        fun logError(error: Throwable, tag: String? = null) {
            val finalTag = tag?.let { "$TAG_PREFIX:$it" } ?: _tag
            Logger.e(finalTag, error)
        }

        fun logError(message: String, error: Throwable? = null, tag: String? = null) {
            val finalTag = tag?.let { "$TAG_PREFIX:$it" } ?: _tag
            if (error != null) {
                Logger.e(finalTag, error) { message }
            } else {
                Logger.e(finalTag) { message }
            }
        }
    }
}