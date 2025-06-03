plugins {
    `kotlin-dsl`
}

group = "com.hillsongptapp.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin{
    plugins {
        register("androidApplication") {
            id = "hillsongptapp.android.application"
            implementationClass = "rfm.hillsongptapp.convention.AndroidApplicationConventionPlugin"
        }

    }
}