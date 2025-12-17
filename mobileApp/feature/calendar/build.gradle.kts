plugins {
    id("kmp-feature-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.network)
            implementation(projects.core.navigation)
            implementation(projects.core.data)
            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.calendar"
}
