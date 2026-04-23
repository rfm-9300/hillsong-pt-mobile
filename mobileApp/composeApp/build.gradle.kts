import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    kotlin("native.cocoapods") version "2.0.0"
}

val apiBaseUrlDebug = providers.gradleProperty("API_BASE_URL_DEBUG").getOrElse("http://172.233.96.224:8080")
val apiBaseUrlRelease = providers.gradleProperty("API_BASE_URL_RELEASE").getOrElse(apiBaseUrlDebug)
val authBaseUrlDebug = providers.gradleProperty("AUTH_BASE_URL_DEBUG").getOrElse(apiBaseUrlDebug)
val authBaseUrlRelease = providers.gradleProperty("AUTH_BASE_URL_RELEASE").getOrElse(apiBaseUrlRelease)

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0"
        summary = "Some description for a Kotlin/Native module"
        homepage = "Link to a Kotlin/Native module homepage"
        ios.deploymentTarget = "16.0"

        podfile = project.file("../iosApp/Podfile")

        framework {
            baseName = "composeApp"
            isStatic = true
        }
    }


    room {
        schemaDirectory("$projectDir/schemas")
    }
    
    sourceSets {

        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.navigation)
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.network)
            implementation(projects.core.preview)
            implementation(projects.core.test)

            implementation(projects.util.logging)
            implementation(projects.util.platform)


            implementation(projects.feature.home)
            implementation(projects.feature.auth)
            implementation(projects.feature.profile)
            implementation(projects.feature.stream)
            implementation(projects.feature.ministries)
            implementation(projects.feature.settings)
            implementation(projects.feature.kids)
            implementation(projects.feature.events)
            implementation(projects.feature.feed)
            implementation(projects.feature.giving)
            implementation(projects.feature.groups)
            implementation(projects.feature.videoPlayer)
            implementation(projects.feature.calendar)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.sqlite.bundled)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            api(libs.koin.core)
            implementation(libs.koin.coroutines)
            implementation(libs.bundles.ktor)
            implementation(libs.bundles.coil)
            implementation(libs.qrose)
        }
    }

}

android {
    namespace = "rfm.hillsongptapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "rfm.hillsongptapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrlDebug\"")
            buildConfigField("String", "AUTH_BASE_URL", "\"$authBaseUrlDebug\"")
            buildConfigField("String", "BUILD_TYPE", "\"debug\"")
            isDebuggable = true
        }
        getByName("release") {
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrlRelease\"")
            buildConfigField("String", "AUTH_BASE_URL", "\"$authBaseUrlRelease\"")
            buildConfigField("String", "BUILD_TYPE", "\"release\"")
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
