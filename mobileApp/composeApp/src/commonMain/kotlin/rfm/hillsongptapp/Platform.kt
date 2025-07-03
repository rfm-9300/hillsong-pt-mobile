package rfm.hillsongptapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform