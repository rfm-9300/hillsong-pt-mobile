@file:Suppress("UnstableApiUsage")

import config.ConfigurationKeys
import ext.configureCompileOptions
import ext.configureDefaultConfig
import ext.configurePlatformTargets
import ext.configureTestOptions
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName

@Suppress("DSL_SCOPE_VIOLATION", "ForbiddenComment") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

android {
    compileSdk = ConfigurationKeys.sdkConfiguration.compileSdk

    configureDefaultConfig()
    configureCompileOptions()
    configureTestOptions()

    namespace = ConfigurationKeys.APPLICATION_ID.plus(".$name")
        .replace("-", "_")

    // Common Android configuration for all libraries
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

kotlin {
    configurePlatformTargets(
        project = project,
        isIosEnabled = true,
    )
}
