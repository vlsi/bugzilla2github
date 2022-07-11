plugins {
    id("bl.java")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    js {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:_")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:_")
            }
        }
    }
}

