rootProject.name = "HillsongPtApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":core")
include(":core:data")
include(":core:designsystem")
include(":core:model")
include(":core:navigation")
include(":core:network")
include(":core:preview")
include(":core:test")

include(":feature")
include(":feature:home")
include(":feature:auth")
include(":feature:settings")
include(":feature:stream")
include(":feature:ministries")
include(":feature:groups")
include(":feature:giving")
include(":feature:feed")
include(":feature:profile")
include(":feature:events")
include(":feature:kids")
include(":feature:prayer")
include(":feature:videoPlayer")
include(":feature:amadeus")
include(":feature:calendar")

include(":util:logging")
include(":util:media")
include(":util:platform")
