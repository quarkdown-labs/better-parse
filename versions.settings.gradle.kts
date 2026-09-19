package com.github.h0tk3y.betterParse.build

import org.gradle.api.plugins.ExtraPropertiesExtension
import kotlin.reflect.full.memberProperties

// Set to the new version
val kotlinVersion = KotlinPlugin.V2420

enum class KotlinPlugin {
    V2420
}

val versions = when (kotlinVersion) {
    KotlinPlugin.V2420 -> Versions(
        // Single source of truth for the release version, bumped by the release workflow.
        version = settings.settingsDir.resolve("version.txt").readText().trim(),
        kotlinVersion = "2.4.20",
        serializationVersion = "1.8.1",
        benchmarkVersion = "0.4.15",
        mavenPublishVersion = "0.37.0"
    )
}

// Register all versions as system properties:

versions.javaClass.kotlin.memberProperties.forEach { property ->
    val value = property.get(versions)
    addGlobalProperty(property.name, value.toString())
}

gradle.allprojects { version = versions.version }

data class Versions(
    val version: String,
    val kotlinVersion: String,
    val serializationVersion: String,
    val benchmarkVersion: String,
    val mavenPublishVersion: String
)

fun addGlobalProperty(key: String, value: String) {
    System.setProperty("build.$key", value)
    fun ExtraPropertiesExtension.addExt() { set(key, value) }
    settings.extensions.extraProperties.addExt()
    gradle.allprojects { extensions.extraProperties.addExt() }
}