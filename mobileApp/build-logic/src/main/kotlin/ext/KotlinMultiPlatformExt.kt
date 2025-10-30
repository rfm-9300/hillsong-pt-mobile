package ext

import config.ConfigurationKeys
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun KotlinMultiplatformExtension.configurePlatformTargets(
    project: Project,
    isIosEnabled: Boolean,
) {
    if (isIosEnabled) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = project.name.replace("-", ".")
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
