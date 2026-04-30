plugins {
    id("kmp-feature-plugin")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation("com.google.mlkit:barcode-scanning:17.3.0")
            implementation("androidx.camera:camera-camera2:1.3.4")
            implementation("androidx.camera:camera-lifecycle:1.3.4")
            implementation("androidx.camera:camera-view:1.3.4")
        }
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.feature.qrcode"
}
