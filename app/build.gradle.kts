plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    //id("com.google.gms.google-services")
    id("com.ncorti.ktfmt.gradle") version "0.16.0"
    kotlin("kapt") // Kotlin annotation processing plugin
    id("com.google.dagger.hilt.android") // Dagger Hilt plugin, used for dependency injection

    // SonarCloud plugin for running static code analysis
    id("org.sonarqube") version "4.4.1.3373"

    // handling secrets.properties
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

    // google firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "ch.epfl.cs311.wanderwave"
    compileSdk = 34

    defaultConfig {
        applicationId = "ch.epfl.cs311.wanderwave"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Spotify API redirect URI: wanderwave-auth://callback
        manifestPlaceholders["redirectSchemeName"] = "wanderwave-auth"
        manifestPlaceholders["redirectHostName"] = "callback"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../release.keystore")
            storePassword = System.getenv("KEYSTORE_RELEASE_PASSWORD")
            keyAlias = "release"
            keyPassword = System.getenv("KEYSTORE_RELEASE_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("org.testng:testng:6.9.6")
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.spotify.auth)
    implementation(libs.gson)
    implementation(files("../libs/spotify-app-remote-release-0.8.0.aar"))

    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation("com.google.firebase:firebase-core:17.0.0")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    implementation("com.google.android.play:core-ktx:1.7.0")

    implementation(libs.maps.compose)

    implementation(libs.play.services.location)
    implementation(libs.accompanist.permissions)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.dagger.hilt.android.compiler)

    testImplementation(libs.dagger.hilt.android.testing)
    kaptTest(libs.google.hilt.android.compiler)

    implementation(libs.androidx.navigation.compose)
    // Hilt Navigation Compose library for injecting ViewModels in Compose
    implementation(libs.androidx.hilt.navigation.compose)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    androidTestImplementation(libs.kaspresso)
    // Allure support
    androidTestImplementation(libs.kaspresso.allure.support)
    // Jetpack Compose support
    androidTestImplementation(libs.kaspresso.compose.support)

    // Dependency for using Intents in instrumented tests
    androidTestImplementation(libs.androidx.espresso.intents)

    // Dependencies for using MockK in instrumented tests
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)

    // Dependencies for the photo part
    implementation(libs.coil.compose)

    androidTestImplementation("io.mockk:mockk:1.13.10")
    androidTestImplementation("io.mockk:mockk-android:1.13.10")
    androidTestImplementation("io.mockk:mockk-agent:1.13.10")

    //Dependencies for Firebase
    implementation ("com.google.firebase:firebase-database-ktx:20.3.1")
}
kapt {
    correctErrorTypes = true
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")

    reports {
        xml.required = true
        html.required = true
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*Hilt*.*",  // Exclude Hilt generated code
        "hilt_aggregated_deps/**",  // Exclude Hilt generated code
        "**/*_Factory.class",  // Exclude Hilt generated code
        "**/*_MembersInjector.class",  // Exclude Hilt generated code
    )
    val debugTree = fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.layout.buildDirectory.get()) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
    })
}

sonar {
    properties {
        property("sonar.projectKey", "G12-Wanderwave_Wanderwave")
        property("sonar.organization", "g12-wanderwave")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        property("sonar.coverage.exclusions", "src/main/java/ch/epfl/cs311/wanderwave/ui/**/*")
    }
}

secrets {
    propertiesFileName = "secrets.properties"

    defaultPropertiesFileName = "local.defaults.properties"

    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}
