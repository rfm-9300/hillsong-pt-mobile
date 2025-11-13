plugins {
    id("kmp-feature-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)
            implementation(projects.core.designsystem)
            implementation(projects.core.network)
            implementation(projects.util.platform)
            implementation(projects.util.logging)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }

        androidMain.dependencies {
            // No external dependencies needed - using WebView for YouTube playback
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.videoplayer"
}
