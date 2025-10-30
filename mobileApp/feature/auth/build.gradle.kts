plugins {
    id("kmp-feature-plugin")
    alias(libs.plugins.ksp)
    kotlin("native.cocoapods") version "2.0.0"
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0"
        summary = "Some description for a Kotlin/Native module"
        homepage = "Link to a Kotlin/Native module homepage"
        ios.deploymentTarget = "16.0"

        pod("GoogleSignIn")

        framework {
            baseName = "feature.auth"
            isStatic = true
        }
    }

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