plugins {
    id("kmp-feature-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.data)
            implementation(projects.core.network)
            implementation(projects.core.navigation)
            implementation(projects.feature.qrcode)
            implementation(projects.util.logging)
            implementation(libs.kotlinx.datetime)
            implementation(libs.qrose)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.koin.core)
            implementation(projects.core.data)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.kids"
}
