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
            implementation(projects.util.media)
            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.events"
}