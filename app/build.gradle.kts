plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.compose)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    kotlin("plugin.serialization") version "1.9.0"
    id("org.sonarqube") version "5.1.0.4882"
    jacoco
}


sonar {
    properties {
        property("sonar.projectKey", "Stravion")
        property("sonar.projectName", "Stravion")
        property("sonar.host.url", "http://192.168.1.172:9000")
        property("sonar.token", "sqp_10bde5825ded07be9e2078a18ef297b227ee2719")

        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.sources", "src/main/java")

        // 👇 This is your fix
        property("sonar.exclusions", "**/*.java")
    }
}


android {
    namespace = "com.outdu.camconnect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.outdu.camconnect"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }

        externalNativeBuild{
            cmake{
                val gstreamerRoot = project.file("src/main/gstreamer_1_0_android_1_22_4").absolutePath
                if (!File(gstreamerRoot).exists()) {
                    throw GradleException("GStreamer path not found: $gstreamerRoot")
                }
                arguments.add("-DANDROID_STL=c++_shared")
                arguments.add("-DGSTREAMER_ROOT_ANDROID=${gstreamerRoot}")
            }
        }
    }

    lint {
        disable += "NullSafeMutableLiveData"
    }

    buildTypes {
        debug {
            isDebuggable = true
//            isProfileable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.10.2"
        }
    }

    ndkVersion = "28.2.13676358"
}

dependencies {


    //MapLibre

    implementation(libs.android.sdk)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio) // or ktor-client-android
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation(libs.androidx.material3.window.size.class1)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.androidx.appcompat.resources)

    // CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation( "androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    // TensorFlow Lite (stable version without conflicts)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    // GPU delegate for hardware acceleration (matching versions)
    implementation(libs.tensorflow.lite.gpu)



    // Lottie Animation
    implementation("com.airbnb.android:lottie-compose:6.1.0")

//    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.commons.net)
    implementation(libs.androidx.window)
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.mlkit.barcode.scanning)
    
    // Navigation Compose
    implementation(libs.androidx.navigation.compose)
    
    // Unit Testing Dependencies
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("org.mockito:mockito-inline:5.1.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    
    // Android Testing Dependencies
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.mockito:mockito-android:5.1.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    
    // Compose Testing
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
    // Test Coverage
    testImplementation("org.jacoco:org.jacoco.core:0.8.8")
}

secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}

// Jacoco configuration for test coverage
jacoco {
    toolVersion = "0.8.8"
}

// Simple coverage report task
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    
    classDirectories.setFrom(
        fileTree(project.buildDir) {
            include(
                "**/classes/**/main/**",
                "**/tmp/kotlin-classes/debug/**"
            )
            exclude(
                "**/R.class",
                "**/R\$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
                "android/**/*.*",
                "**/*\$ViewInjector*.*",
                "**/*\$ViewBinder*.*",
                "**/databinding/*",
                "**/android/databinding/*",
                "**/androidx/databinding/*",
                "**/BR.*"
            )
        }
    )
    
    sourceDirectories.setFrom(files(
        "${project.projectDir}/src/main/java",
        "${project.projectDir}/src/main/kotlin"
    ))
    
    executionData.setFrom(fileTree(project.buildDir) {
        include("jacoco/testDebugUnitTest.exec")
    })
}