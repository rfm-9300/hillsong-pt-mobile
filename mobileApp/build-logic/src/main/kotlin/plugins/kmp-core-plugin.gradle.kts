@file:Suppress("UnstableApiUsage")

import ext.getVersionCatalog
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposeExtension
import org.gradle.kotlin.dsl.getByType

@Suppress("DSL_SCOPE_VIOLATION", "ForbiddenComment")
plugins {
    id("kmp-library-compose-plugin")
}

// Add common core module dependencies
kotlin {
    sourceSets {
        commonMain.dependencies {
            val libs = getVersionCatalog()
            val compose = project.extensions.getByType<ComposeExtension>().dependencies

            // Basic Compose dependencies for core modules
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }

        androidMain.dependencies {
            val libs = getVersionCatalog()
            implementation(libs.findLibrary("androidx.ui.tooling").get())
        }
    }
}
