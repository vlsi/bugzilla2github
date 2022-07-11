rootProject.name = "bugzilla2github"

pluginManagement {
    includeBuild("build-logic")
    plugins {
        kotlin("multiplatform") version "1.7.10"
        kotlin("plugin.serialization") version "1.7.10"
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.40.2"
}

include(":bugzilla-common")
include(":bugzilla-backend")
include(":bugzilla-frontend")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
