import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode

plugins {
    application
    id("bl.kotlin-library")
    kotlin("plugin.serialization")
}

val frontendJs by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    frontendJs(projects.bugzillaFrontend.apply { targetConfiguration = "distributionJs" })

    implementation(projects.bugzillaCommon)

    implementation(platform("io.ktor:ktor-bom:_"))

    implementation("de.brudaswen.kotlinx.serialization:kotlinx-serialization-csv:_")

    implementation("io.github.pdvrieze.xmlutil:serialization:_")
    implementation("com.github.ajalt.clikt:clikt:_")
    implementation("com.typesafe:config:_")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:_")
    implementation(platform("org.jetbrains.exposed:exposed-bom:_"))
    implementation("org.jetbrains.exposed:exposed-core")
    implementation("org.jetbrains.exposed:exposed-jdbc")
    implementation("org.jetbrains.xodus:dnq:_")
    implementation("org.jetbrains.xodus:xodus-openAPI:_")
    implementation("org.jetbrains.xodus:java-8-time:_")
    implementation("mysql:mysql-connector-java:_")

    implementation("ch.qos.logback:logback-classic:_") {
        because("Need to adjust debug on/off")
    }

    testImplementation("org.jetbrains.kotlin:kotlin-test:_")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:_")
}

application {
    mainClass.set("io.github.vlsi.bugzilla.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

if (project.hasProperty("withFrontend")) {
    tasks.processResources {
        into("static") {
            from(frontendJs) {
                exclude("index.html")
            }
        }
    }
}
