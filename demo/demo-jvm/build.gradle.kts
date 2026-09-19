plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(rootProject)
    implementation(kotlin("stdlib"))
}