
plugins {
    id("kmp-library-plugin")
}
kotlin{

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "util.logging"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kermit)
            implementation(libs.koin.core)
        }
    }

}

android {
    namespace = "rfm.hillsongptapp.util.logging"
}