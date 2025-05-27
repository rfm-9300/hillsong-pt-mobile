package rfm.activehive

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform