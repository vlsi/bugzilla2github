import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    id("bl.repositories")
    kotlin("js")
    kotlin("plugin.serialization")
}

kotlin {
    js {
        // To build distributions for and run tests on browser or Node.js use one or both of:
        browser()
        binaries.executable()
    }
}

fun kwrappers(target: String): String =
    "org.jetbrains.kotlin-wrappers:kotlin-$target"

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(projects.bugzillaCommon)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:_")

    implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:_"))
    implementation(kwrappers("react"))
    implementation(kwrappers("react-dom"))
    implementation(kwrappers("react-router-dom"))
    implementation(kwrappers("css"))
    implementation(kwrappers("react-query"))
}

val browserProductionWebpack by tasks.existing(KotlinWebpack::class) {
    dependsOn("developmentExecutableCompileSync")
}
val browserDevelopmentWebpack by tasks.existing(KotlinWebpack::class) {
    dependsOn("productionExecutableCompileSync")
}

val distributionJs by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

(artifacts) {
    distributionJs(browserDevelopmentWebpack.map { it.destinationDirectory })
}
