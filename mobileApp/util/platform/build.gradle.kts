plugins {
    id("kmp-library-plugin")
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "util.platform"
            isStatic = true
        }
    }
    
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
