plugins {
    id("kmp-feature-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)
            implementation(projects.core.designsystem)
            implementation(projects.util.platform)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.videoplayer"
}
