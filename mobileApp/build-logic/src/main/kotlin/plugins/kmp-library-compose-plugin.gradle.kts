@file:Suppress("UnstableApiUsage")

import ext.getVersionCatalog
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.apply

@Suppress("DSL_SCOPE_VIOLATION", "ForbiddenComment")
plugins {
    id("kmp-library-plugin")
}

// Apply Compose plugins programmatically
apply(plugin = "org.jetbrains.compose")
apply(plugin = "org.jetbrains.kotlin.plugin.compose")

// Add common Compose dependencies
kotlin {
    sourceSets {
        commonMain.dependencies {
            val libs = getVersionCatalog()
            implementation(libs.findLibrary("androidx.lifecycle.runtime.compose").get())
            implementation(libs.findLibrary("androidx.lifecycle.viewmodel").get())
        }
    }
}
