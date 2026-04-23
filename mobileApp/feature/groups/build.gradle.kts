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
            implementation(projects.core.network)
            implementation(projects.util.media)
            implementation(projects.util.platform)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.groups"
}
