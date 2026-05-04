import java.util.Properties

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.app.distribution) apply false
    kotlin("native.cocoapods") version "2.0.0"
}

// ─── Firebase App Distribution ───────────────────────────────────────────────

val localProps = Properties().apply {
    val f = file("local.properties")
    if (f.exists()) load(f.inputStream())
}

// Shortcut task: builds the debug APK then uploads to Firebase App Distribution
tasks.register("distributeAndroid") {
    description = "Distribute Android debug APK to Firebase App Distribution"
    group = "distribution"
    dependsOn(":composeApp:assembleDebug", ":composeApp:appDistributionUploadDebug")
}

// iOS: archive → export IPA → firebase distribute
tasks.register<Exec>("distributeIos") {
    description = "Build iOS app archive and distribute to Firebase App Distribution"
    group = "distribution"

    val appleTeamId = localProps.getProperty("APPLE_TEAM_ID", "")
    val firebaseTesters = localProps.getProperty("FIREBASE_TESTERS", "")
    val archivePath = "${rootDir.absolutePath}/build/iosApp.xcarchive"
    val exportPath = "${rootDir.absolutePath}/build/iosApp-dist"
    val exportOptions = "${rootDir.absolutePath}/iosApp/ExportOptions.plist"
    val iosAppId = "1:173674468850:ios:90c5b7c07a3155df28876b"

    val teamArg = if (appleTeamId.isNotEmpty()) "DEVELOPMENT_TEAM=$appleTeamId" else ""
    val testersArg = if (firebaseTesters.isNotEmpty()) "--testers \"$firebaseTesters\"" else ""

    workingDir(file("iosApp"))
    commandLine(
        "bash", "-c", """
        set -e
        echo "▶ Building iOS archive..."
        xcodebuild archive \
          -workspace iosApp.xcworkspace \
          -scheme iosApp \
          -configuration Release \
          -archivePath "$archivePath" \
          CODE_SIGN_STYLE=Automatic \
          $teamArg

        echo "▶ Exporting IPA..."
        xcodebuild -exportArchive \
          -archivePath "$archivePath" \
          -exportOptionsPlist "$exportOptions" \
          -exportPath "$exportPath" \
          -allowProvisioningUpdates

        echo "▶ Distributing to Firebase App Distribution..."
        IPA_FILE=${'$'}(find "$exportPath" -name "*.ipa" | head -1)
        firebase appdistribution:distribute "${'$'}IPA_FILE" \
          --app "$iosAppId" \
          --release-notes "Build ${'$'}(date +%Y-%m-%d\ %H:%M)" \
          $testersArg

        echo "✅ iOS distribution complete!"
        """.trimIndent()
    )
}
