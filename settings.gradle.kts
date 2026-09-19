@file:Suppress("UnstableApiUsage")

pluginManagement {
    abstract class RepositorySetup :
        BuildServiceParameters, (RepositoryHandler, Boolean) -> Unit, BuildService<RepositorySetup> {
        override fun invoke(repositories: RepositoryHandler, isPlugins: Boolean): Unit = with(repositories) {
            mavenCentral()
            if (isPlugins) {
                gradlePluginPortal()
            }
        }
    }

    val configureRepositories = gradle.sharedServices.registerIfAbsent("repositories", RepositorySetup::class) { }.get()

    configureRepositories(repositories, true)
    gradle.allprojects { configureRepositories(repositories, false) }

    apply(from = "versions.settings.gradle.kts")
    val kotlinVersion: String by settings
    val benchmarkVersion: String by settings
    val mavenPublishVersion: String by settings

    plugins {
        kotlin("multiplatform").version(kotlinVersion)
        kotlin("plugin.allopen").version(kotlinVersion)
        id("org.jetbrains.kotlinx.benchmark").version(benchmarkVersion)
        id("com.vanniktech.maven.publish").version(mavenPublishVersion)
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "better-parse"

// Don't include demos and benchmarks in a composite build
val isCompositeBuild: Boolean = gradle.parent != null
if (!isCompositeBuild) {
    include(":benchmarks", ":demo:demo-jvm", ":demo:demo-js", ":demo:demo-native")
}
