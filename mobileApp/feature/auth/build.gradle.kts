plugins {
    id("kmp-feature-plugin")
    alias(libs.plugins.ksp)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.dependencies {
            implementation("androidx.credentials:credentials:1.5.0")
            implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
            implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(projects.core.navigation)
            implementation(projects.core.designsystem)
            implementation(projects.core.network)
            implementation(projects.util.logging)
            implementation(projects.core.data)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.auth"
} 