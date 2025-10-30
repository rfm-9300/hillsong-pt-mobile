plugins {
    id("kmp-library-compose-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kermit)
            implementation(libs.koin.core)
            implementation(libs.bundles.ktor)
            implementation(libs.bundles.coil)
            implementation(compose.material3)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.util.media"
}