rootProject.name = "bugzilla2github"

pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("de.fayard.refreshVersions") version "0.40.2"
}

include(":bugzilla-export")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
