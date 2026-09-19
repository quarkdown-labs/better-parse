plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        nodejs()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(rootProject)
            }
        }
    }
}

val jsMainCompilation = kotlin.js().compilations.getByName("main")

val assembleWeb = tasks.register<Sync>("assembleWeb") {
    from(project.provider {
        jsMainCompilation.compileDependencyFiles.files.filter { it.isFile }.map(::zipTree).map {
            it.matching {
                include("*.js")
                exclude("**/META-INF/**")
            }
        }
    })

    from(jsMainCompilation.compileTaskProvider.map { it.destinationDirectory })
    from(jsMainCompilation.defaultSourceSet.resources) { include("*.html") }
    into(layout.buildDirectory.dir("web"))
}

tasks.assemble {
    dependsOn(assembleWeb)
}
