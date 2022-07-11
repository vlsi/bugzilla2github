import buildlogic.filterEolSimple

plugins {
    `java-library`
    id("bl.java")
    id("bl.testing")
}

tasks.withType<JavaCompile>().configureEach {
    inputs.property("java.version", System.getProperty("java.version"))
    inputs.property("java.vm.version", System.getProperty("java.vm.version"))
    options.apply {
        encoding = "UTF-8"
        compilerArgs.add("-Xlint:deprecation")
        compilerArgs.add("-Werror")
    }
}

tasks.withType<Jar>().configureEach {
    into("META-INF") {
        filterEolSimple("crlf")
        from("$rootDir/LICENSE.txt")
        from("$rootDir/NOTICE")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    manifest {
        // providers.gradleProperty does not work
        // see https://github.com/gradle/gradle/issues/14972
        val name = rootProject.findProperty("project.name")
        val vendor = rootProject.findProperty("project.vendor.name")
        attributes(mapOf(
            "Specification-Title" to name,
            "Specification-Version" to project.version,
            "Specification-Vendor" to vendor,
            "Implementation-Title" to name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to vendor,
            "Implementation-Vendor-Id" to rootProject.findProperty("project.vendor.id"),
            "Implementation-Url" to rootProject.findProperty("project.url"),
        ))
    }
}
