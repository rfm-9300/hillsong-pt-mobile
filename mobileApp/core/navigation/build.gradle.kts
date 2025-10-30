plugins {
    id("kmp-core-plugin")
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)

            api(libs.koin.core)
            api(libs.koin.compose)
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.core.navigation"
}