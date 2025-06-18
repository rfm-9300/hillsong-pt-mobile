import org.gradle.kotlin.dsl.dependencies
import org.gradle.api.Plugin
import org.gradle.api.Project

class CmpLibraryPlugin:  Plugin<Project> {
    override fun apply(target: org.gradle.api.Project) {
        with(target) {
            pluginManager.apply {
                apply("org.jetbrains.kotlin.multiplatform")

            }

            dependencies {
                add("implementation", "androidx.core:core-ktx:1.6.0")
                add("implementation", "androidx.appcompat:appcompat:1.3.1")
                add("implementation", "com.google.android.material:material:1.4.0")
                add("implementation", "androidx.constraintlayout:constraintlayout:2.0.4")
            }
        }
    }
}