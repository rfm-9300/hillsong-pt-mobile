package ext

import config.ConfigurationKeys
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun KotlinMultiplatformExtension.configurePlatformTargets(
    isIosEnabled: Boolean,
) {
    if (isIosEnabled) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = ConfigurationKeys.APP_NAME
                isStatic = true
            }
        }
    }
    androidTarget {
        compilerOptions {
            jvmTarget.set(
                ConfigurationKeys.javaConfiguration.jvmTarget,
            )
        }
    }
}
