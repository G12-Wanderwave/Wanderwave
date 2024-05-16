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

        testInstrumentationRunner = "ch.epfl.cs311.wanderwave.CustomTestRunner"

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
    // AndroidX
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

    // Firebase
    implementation(libs.firebase.firestore.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation ("com.google.firebase:firebase-database-ktx:20.3.1")

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.coroutines)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.dagger.hilt.android.compiler)
    testImplementation(libs.dagger.hilt.android.testing)
    kaptTest(libs.google.hilt.android.compiler)
    // Glide
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    kapt ("com.github.bumptech.glide:compiler:4.12.0")
    // Room
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Other libraries
    implementation(libs.spotify.auth)
    implementation(libs.gson)
    implementation(files("../libs/spotify-app-remote-release-0.8.0.aar"))
    implementation("com.google.android.play:core-ktx:1.7.0")
    implementation(libs.maps.compose)
    implementation(libs.places)
    implementation(libs.play.services.location)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("org.testng:testng:6.9.6")
    androidTestImplementation(libs.kaspresso)
    androidTestImplementation(libs.kaspresso.allure.support)
    androidTestImplementation(libs.kaspresso.compose.support)
    androidTestImplementation(libs.androidx.espresso.intents)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)
    androidTestImplementation ("junit:junit:4.13.2")
    androidTestImplementation("org.mockito:mockito-android:3.11.2")


    androidTestImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1")

    // Debugging
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
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
