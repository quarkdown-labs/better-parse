@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
}

kotlin {
    jvmToolchain(21)

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    js {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    linuxX64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    mingwX64()

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        // Shared Native Scope
        val nativeMain by creating {
            dependsOn(commonMain)
        }

        // Link all native targets to 'nativeMain'
        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
            compilations["main"].defaultSourceSet.dependsOn(nativeMain)
        }

        // JS
        val jsMain by getting {
            dependsOn(commonMain)
        }
        val jsTest by getting {
            dependsOn(commonTest)
        }

        // Wasm
        val wasmJsMain by getting {
            dependsOn(commonMain)
        }
        val wasmJsTest by getting {
            dependsOn(commonTest)
        }
    }
}

// Don't include test binaries in a composite build
val isCompositeBuild: Boolean = gradle.parent != null
if (isCompositeBuild) {
    // Disable all Kotlin/Native test binaries tasks (e.g. iosSimulatorArm64TestBinaries)
    // but keep main binaries available for consuming builds.
    tasks.configureEach {
        val n = name
        if (n.endsWith("TestBinaries") || n.contains("Test", ignoreCase = false) && n.contains("Binaries")) {
            enabled = false
        }
    }
}

mavenPublishing {
    coordinates(group.toString(), "better-parse", version.toString())

    publishToMavenCentral()

    // Kept conditional so that local builds and publishToMavenLocal work without a signing key.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }

    pom {
        name.set("better-parse")
        description.set("A nice parser combinator library for Kotlin, with Kotlin/Wasm support")
        url.set("https://github.com/quarkdown-labs/better-parse")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("h0tk3y")
                name.set("Dmitry Kichinsky")
                url.set("https://github.com/h0tk3y")
            }
            developer {
                id.set("iamgio")
                name.set("Giorgio Garofalo")
                url.set("https://github.com/iamgio")
            }
        }
        scm {
            url.set("https://github.com/quarkdown-labs/better-parse")
            connection.set("scm:git:git://github.com/quarkdown-labs/better-parse.git")
            developerConnection.set("scm:git:ssh://git@github.com/quarkdown-labs/better-parse.git")
        }
    }
}
