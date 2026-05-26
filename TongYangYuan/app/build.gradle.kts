plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.tongyangyuan"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tongyangyuan"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("tongyangyuan.keystore")
            storePassword = "tongyangyuan2024"
            keyAlias = "tongyangyuan"
            keyPassword = "tongyangyuan2024"
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
            signingConfig = signingConfigs.getByName("release")
        }
    }

    // Set the source and target compatibility to Java 17.
    // The build will now depend on the JDK version used by Gradle.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.fragment)
    implementation(libs.annotation)
    implementation(libs.core)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    implementation(libs.spinkit)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.gson)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.lifecycle.runtime.ktx)

    // OpenIM Android SDK - 即时通讯
    implementation("io.openim:android-sdk:3.8.3.2@aar") {
        isTransitive = true
    }
    // AgentWeb for WebView
    implementation("com.github.Justson.AgentWeb:agentweb-core:v5.0.0-alpha.1-androidx")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // XXPermissions for permission handling
    implementation("com.github.getActivity:XXPermissions:18.6")

    // OkHttp for WebSocket (STOMP信令 + 视频通话信令)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // WebRTC for video call (from task project)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))
}
