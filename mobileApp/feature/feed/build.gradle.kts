plugins {
    id("kmp-feature-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.network)
            implementation(projects.util.media)
            implementation(projects.util.logging)
            implementation(projects.core.data)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.feed"
}