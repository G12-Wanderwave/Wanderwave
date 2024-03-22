// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false

    // Dagger Hilt plugin, used for dependency injection
    id("com.google.dagger.hilt.android") version "2.49" apply false
    // SonarCloud plugin for running static code analysis
    id("org.sonarqube") version "4.4.1.3373"
}

sonar {
    properties {
        property("sonar.projectKey", "G12-Wanderwave_Wanderwave")
        property("sonar.organization", "g12-wanderwave")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}
