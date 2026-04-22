plugins {
    id("kmp-feature-plugin")
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)
            implementation(projects.core.network)
            implementation(projects.core.designsystem)
            implementation(projects.core.data)

            implementation(projects.feature.profile)

            implementation(projects.util.media)
            implementation(projects.util.logging)
            implementation(projects.util.platform)
            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.home"
}