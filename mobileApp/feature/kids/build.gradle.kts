plugins {
    id("kmp-feature-plugin")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            // ML Kit for QR code scanning
            implementation("com.google.mlkit:barcode-scanning:17.3.0")
            // CameraX for camera preview
            implementation("androidx.camera:camera-camera2:1.3.4")
            implementation("androidx.camera:camera-lifecycle:1.3.4")
            implementation("androidx.camera:camera-view:1.3.4")
        }
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.data)
            implementation(projects.core.network)
            implementation(projects.core.navigation)
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