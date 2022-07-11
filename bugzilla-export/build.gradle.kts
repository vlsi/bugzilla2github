plugins {
    `application`
    id("bl.kotlin-library")
    kotlin("plugin.serialization") version "1.7.10"
}

dependencies {
    implementation(platform("io.ktor:ktor-bom:_"))

    implementation("de.brudaswen.kotlinx.serialization:kotlinx-serialization-csv:_")
    implementation("io.github.pdvrieze.xmlutil:serialization:_")
    implementation("com.github.ajalt.clikt:clikt:_")
    implementation("io.ktor:ktor-client-cio")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:_")
    implementation("org.jetbrains.xodus:dnq:_")
    implementation("org.jetbrains.xodus:xodus-openAPI:_")
    implementation("org.jetbrains.xodus:java-8-time:_")
    runtimeOnly("ch.qos.logback:logback-classic:_")
}

application {
    mainClass.set("io.github.vlsi.bz.MainKt")
}

// https://bz.apache.org/bugzilla/buglist.cgi?product=JMeter&query_format=advanced&limit=10&ctype=csv
