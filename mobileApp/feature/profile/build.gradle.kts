plugins {
    id("kmp-feature-plugin")
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.data)
            implementation(projects.core.navigation)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.profile"
}