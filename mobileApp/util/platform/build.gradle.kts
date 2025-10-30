plugins {
    id("kmp-library-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Add common dependencies if needed
        }

        androidMain.dependencies {
            // Android-specific dependencies
        }

        iosMain.dependencies {
            // iOS-specific dependencies
        }
    }
}

android {
    namespace = "rfm.hillsongptapp.util.platform"
}
