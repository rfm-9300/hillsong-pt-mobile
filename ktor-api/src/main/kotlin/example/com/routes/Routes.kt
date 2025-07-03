package example.com.routes

object Routes {

    /**
     * Routes for the UI
     **/

    object Ui {
        object Event {
            const val LIST_PAST = "/events/past"
            const val LIST_UPCOMING = "/events/upcoming"
            const val CREATE = "/events/create"
            const val DETAILS = "/events/{eventId}"
            const val UPDATE = "/events/update/{eventId}"
            const val DELETE = "/events/delete/{eventId}"
        }
        object Home {
            const val HOME = "/home"
            const val PROFILE_MENU = "/profile-menu"
            const val CREATE_POST = "/post/create"
        }
        object Auth {
            const val LOGIN = "/login"
            const val SIGNUP = "/signup"
            const val FORGOT_PASSWORD = "/forgot-password"
            const val RESET_PASSWORD = "/reset-password"
            const val RESET_PASSWORD_WITH_TOKEN = "/reset-password/{token}"
        }

        object Profile {
            const val ROOT = "/profile"
        }
    }

    /**
     * Routes for the API
     **/

    object Api {
        object Event {
            const val CREATE = "/api/events"
            const val GET = "/api/events/{id}"
            const val UPDATE = "/api/events/update"
            const val DELETE = "/api/events/delete"
            const val LIST = "/api/events"
            const val JOIN_EVENT = "/api/events/join"
            const val APPROVE_USER = "/api/events/approve"
            const val REMOVE_USER = "/api/events/remove-user"
        }

        object Post {
            const val CREATE = "/api/posts"
            const val GET = "/api/posts/{id}"
            const val UPDATE = "/api/posts/update"
            const val DELETE = "/api/posts/delete"
            const val LIST = "/api/posts"
        }

        object Auth {
            const val LOGIN = "/api/auth/login"
            const val GOOGLE_LOGIN = "/api/auth/google-login"
            const val FACEBOOK_LOGIN = "/api/auth/facebook-login"
            const val SIGNUP = "/api/auth/signup"
            const val VERIFY = "/api/auth/verify"
            const val REQUEST_PASSWORD_RESET = "/api/auth/request-password-reset"
            const val RESET_PASSWORD = "/api/auth/reset-password"
        }

        object Profile {
            const val GET = "/api/profile"
            const val UPDATE = "/api/profile/update"
        }
    }

    object DynamicJs{
        const val API_CLIENT = "/js/ApiClient.js"
    }

    object Sse {
        const val SSE_CONNECTION = "/sse"
    }

    object Placeholder {
        val PLACEHOLDERS: Map<String, String> = mapOf(
            "%%API_CREATE_EVENT%%" to Api.Event.CREATE,
            "%%API_GET_EVENT%%" to Api.Event.GET,
            "%%API_UPDATE_EVENT%%" to Api.Event.UPDATE,
            "%%API_DELETE_EVENT%%" to Api.Event.DELETE,
            "%%API_LIST_EVENTS%%" to Api.Event.LIST,
            "%%SSE_CONNECTION%%" to Sse.SSE_CONNECTION,
            "%%API_LOGIN%%" to Api.Auth.LOGIN,
            "%%API_GOOGLE_LOGIN%%" to Api.Auth.GOOGLE_LOGIN,
            "%%API_FACEBOOK_LOGIN%%" to Api.Auth.FACEBOOK_LOGIN,
            "%%API_SIGNUP%%" to Api.Auth.SIGNUP,
            "%%API_JOIN_EVENT%%" to Api.Event.JOIN_EVENT,
            "%%PROFILE_MENU%%" to Ui.Home.PROFILE_MENU,
            "%%API_CREATE_POST%%" to Api.Post.CREATE,
            "%%API_GET_POST%%" to Api.Post.GET,
            "%%API_UPDATE_POST%%" to Api.Post.UPDATE,
            "%%API_DELETE_POST%%" to Api.Post.DELETE,
            "%%UI_HOME%%" to Ui.Home.HOME,
            "%%UI_EVENTS_PAST%%" to Ui.Event.LIST_PAST,
            "%%UI_EVENTS_UPCOMING%%" to Ui.Event.LIST_UPCOMING,
            "%%API_UPDATE_PROFILE%%" to Api.Profile.UPDATE,
            "%%API_APPROVE_USER%%" to Api.Event.APPROVE_USER,
            "%%API_REMOVE_USER%%" to Api.Event.REMOVE_USER,
            "%%UI_EVENT_DETAIL%%" to Ui.Event.DETAILS,
            "%%API_REQUEST_PASSWORD_RESET%%" to Api.Auth.REQUEST_PASSWORD_RESET,
            "%%API_RESET_PASSWORD%%" to Api.Auth.RESET_PASSWORD,
            "%%UI_FORGOT_PASSWORD%%" to Ui.Auth.FORGOT_PASSWORD,
            "%%UI_RESET_PASSWORD%%" to Ui.Auth.RESET_PASSWORD,
            "%%UI_RESET_PASSWORD_WITH_TOKEN%%" to Ui.Auth.RESET_PASSWORD_WITH_TOKEN
        )
    }
}

