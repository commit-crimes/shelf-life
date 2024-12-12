import java.io.FileInputStream
import java.util.Properties

plugins {
    jacoco
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.gms)
    alias(libs.plugins.sonar)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.android.shelfLife"
    compileSdk = 34

    // Load the API key from local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    val openAIApiKey: String = localProperties.getProperty("OPENAI_API_KEY") ?: ""

    defaultConfig {
        applicationId = "com.android.shelfLife"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Use HiltTestRunner for instrumentation tests
        testInstrumentationRunner = "com.android.shelfLife.HiltTestRunner"
        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Add the API key to the BuildConfig class
        buildConfigField(
            type = "String",
            name = "OPENAI_API_KEY",
            value = "\"${openAIApiKey}\""
        )
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    signingConfigs{
        val isCI = System.getenv("GITHUB_ACTIONS")?.toBoolean() ?: false
        if(isCI && System.getenv("GITHUB_WORKFLOW") == "Build Android APK"){
            named("debug"){
                storeFile = file("../keystore.jks")
                storePassword = System.getenv("DEBUG_KEYSTORE_PASSWORD")
                keyAlias  = System.getenv("DEBUG_KEY_ALIAS")
                keyPassword  = System.getenv("DEBUG_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            pickFirsts += "META-INF/DEPENDENCIES"
        }
    }

    // Robolectric tests setup
    sourceSets.getByName("testDebug") {
        val test = sourceSets.getByName("test")
        java.setSrcDirs(test.java.srcDirs)
        res.setSrcDirs(test.res.srcDirs)
        resources.setSrcDirs(test.resources.srcDirs)
    }

    sourceSets.getByName("test") {
        java.setSrcDirs(emptyList<File>())
        res.setSrcDirs(emptyList<File>())
        resources.setSrcDirs(emptyList<File>())
    }

}

// Jacoco test configuration
tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}



sonar {
    properties {
        property("sonar.projectKey", "commit-crimes")
        property("sonar.projectName", "shelf-life")
        property("sonar.organization", "commit-crimes")
        property("sonar.host.url", "https://sonarcloud.io")
        // Comma-separated paths to the various directories containing the *.xml JUnit report files. Each path may be absolute or relative to the project base directory.
        property(
            "sonar.junit.reportPaths",
            "${project.layout.buildDirectory.get()}/test-results/testDebugUnitTest"
        )
        // Paths to xml files with Android Lint issues. If the main flavor is changed, this file will have to be changed too.
        property(
            "sonar.androidLint.reportPaths",
            "${project.layout.buildDirectory.get()}/reports/lint-results-debug.xml"
        )
        // Paths to JaCoCo XML coverage report files.
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
        )
    }
}

// Jacoco test report task
tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )
    val debugTree = fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.buildDir) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
    })
}

// Ensure ktfmtCheck tasks depend on their corresponding ktfmtFormat tasks
tasks.named("ktfmtCheckAndroidTest") {
    dependsOn(tasks.named("ktfmtFormatAndroidTest"))
}

tasks.named("ktfmtCheckMain") {
    dependsOn(tasks.named("ktfmtFormatMain"))
}

tasks.named("ktfmtCheckTest") {
    dependsOn(tasks.named("ktfmtFormatTest"))
}

// Task to copy APKs to app/releases/
tasks.register<Copy>("copyApks") {
    // Ensure the APK is built before copying
    dependsOn("assembleDebug")

    val sourceDir = "$buildDir/outputs/apk/debug/"
    val destinationDir = "$projectDir/releases/"

    from(sourceDir)
    into(destinationDir)

    include("*.apk")

    doFirst {
        println("Listing APK files in source directory: $sourceDir")
        fileTree(sourceDir).forEach {
            println("Found file: ${it.name}")
        }
    }

    doLast {
        println("APKs copied to $destinationDir")
        fileTree(destinationDir).forEach {
            println("Copied APK: ${it.name}")
        }
    }
}

// Dependencies
dependencies {
    // Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.kotlinx.serialization.json)

    // Jetpack Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.navigation.testing)
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.test.core.ktx)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.material)
    implementation(libs.androidx.material.icons.extended)

    // OpenAI
    implementation(libs.aallam.openai.client)
    implementation(libs.ktor.client.apache5)

    // Barcode Scanner
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.guava)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Google Service
    implementation(libs.play.services.auth)

    // Firebase
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation(platform(libs.firebase.bom))

    // Networking with OkHttp
    implementation(libs.okhttp)

    // Image from network
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // Hilt Testing
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.android.compiler)
    implementation(libs.androidx.runner)
    implementation(libs.hilt.android.testing)

    // Unit Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)
    testImplementation(libs.json)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockk)

    // Test UI
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    //androidTestImplementation(libs.kaspresso)
    //androidTestImplementation(libs.kaspresso.allure.support)
    //androidTestImplementation(libs.kaspresso.compose.support)

    // Coroutine Testing
    testImplementation(libs.kotlinx.coroutines.test)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
}

kapt {
    correctErrorTypes = true
}