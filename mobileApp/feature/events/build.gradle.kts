plugins {
    id("kmp-feature-plugin")
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.data)
            implementation(projects.core.network)
            implementation(projects.core.navigation)
            implementation(projects.feature.qrcode)
            implementation(projects.util.media)
            implementation(libs.kotlinx.datetime)
            implementation(libs.qrose)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.events"
}
