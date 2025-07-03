val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val h2_version: String by project
val exposed_version: String by project
val commons_codec_version: String by project
val koinVersion: String by project
val jakarta_mail_version = "2.0.1"

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.ktor.plugin") version "3.0.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    id("com.google.cloud.tools.jib") version "3.2.1"
}

group = "example.com"
version = file("version.txt").readText().trim()

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

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}
ktor {
    fatJar {
        archiveFileName.set("rfm.ktor-server.jar")
    }
    docker {
        jreVersion.set(JavaVersion.VERSION_19)
    }
    jib {
        from {
            image = "openjdk:19-jdk-alpine"
        }
        to {
            image = "rfm9300/ktor-central"
            tags = setOf("${project.version}")
        }
    }
}


dependencies {
    // Ktor core
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("commons-codec:commons-codec:$commons_codec_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    
    // Ktor HTTP client engines
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    
    // web
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("io.ktor:ktor-server-sse:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Database
    implementation("org.flywaydb:flyway-core:9.20.1")
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("com.h2database:h2:$h2_version")
    // exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:0.30.1")


    // dotenv
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2") // Adjust the version if necessary

    // Email
    implementation("com.sun.mail:jakarta.mail:$jakarta_mail_version")
    implementation("jakarta.activation:jakarta.activation-api:2.0.1")
    
    // SSL for development
    implementation("io.ktor:ktor-network-tls-certificates:$ktor_version")

    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    // Koin
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")
}

