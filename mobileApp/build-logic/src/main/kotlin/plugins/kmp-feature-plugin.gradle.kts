@file:Suppress("UnstableApiUsage")

import ext.getVersionCatalog
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposeExtension
import org.gradle.kotlin.dsl.getByType

@Suppress("DSL_SCOPE_VIOLATION", "ForbiddenComment")
plugins {
    id("kmp-library-compose-plugin")
}

// Add common feature module dependencies
kotlin {
    sourceSets {
        commonMain.dependencies {
            val libs = getVersionCatalog()
            val compose = project.extensions.getByType<ComposeExtension>().dependencies

            // Compose dependencies
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Navigation
            implementation(libs.findLibrary("navigation.compose").get())

            // Koin dependencies for features
            implementation(libs.findLibrary("koin.compose").get())
            implementation(libs.findLibrary("koin.compose.viewmodel").get())
            implementation(libs.findLibrary("koin.coroutines").get())
            api(libs.findLibrary("koin.core").get())
            api(libs.findLibrary("koin.compose").get())
        }

        androidMain.dependencies {
            val libs = getVersionCatalog()
            implementation(libs.findLibrary("androidx.ui.tooling").get())
        }
    }
}
