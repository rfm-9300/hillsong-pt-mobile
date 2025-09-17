plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.google.cloud.tools.jib") version "3.4.0"
}


group = "rfm.com"
version = file("version.txt").readText().trim()

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        kotlin {
            exclude("**/data/**")
            exclude("**/di/**")
            exclude("**/plugins/**")
            exclude("**/routes/**")
            exclude("**/useCases/**")
            exclude("**/services/AttendanceService.kt")
        }
    }
}

val incrementVersion by tasks.registering {
    doLast {
        val versionFile = file("version.txt")
        val currentVersion = versionFile.readText().trim()

        val versionParts = currentVersion.split(".")
        val majorVersion = versionParts[0].toInt()
        val minorVersion = versionParts[1].toInt()
        val patchVersion = versionParts[2].toInt()

        val newPatchVersion = patchVersion + 1
        val newVersion = "$majorVersion.$minorVersion.$newPatchVersion"

        project.version = newVersion
        versionFile.writeText(newVersion)

        println("Version incremented to: $newVersion")
    }
}

tasks.named("jib") {
    dependsOn(incrementVersion)
}

repositories {
    mavenCentral()
}

jib {
    from {
        image = "openjdk:17-jdk-alpine"
    }
    to {
        image = "rfm9300/spring-boot-central"
        tags = setOf("${project.version}")
    }
}


dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Kotlin Support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // Auth0 JWT (for backward compatibility with existing Ktor code)
    implementation("com.auth0:java-jwt:4.4.0")
    
    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("com.h2database:h2")
    
    // OAuth2 Support
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    
    // Utilities
    implementation("commons-codec:commons-codec:1.15")
    
    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    
    // Koin (for backward compatibility with existing code)
    implementation("io.insert-koin:koin-core:3.5.3")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

