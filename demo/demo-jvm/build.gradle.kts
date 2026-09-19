plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(rootProject)
    implementation(kotlin("stdlib"))
}