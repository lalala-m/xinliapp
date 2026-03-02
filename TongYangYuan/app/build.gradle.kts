plugins {
    alias(libs.plugins.android.application)
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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Set the source and target compatibility to Java 17.
    // The build will now depend on the JDK version used by Gradle.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    implementation(libs.spinkit)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.gson)

    // LiveKit SDK
    implementation("io.livekit:livekit-android:2.6.0")

    // AgentWeb for WebView
    implementation("com.github.Justson.AgentWeb:agentweb-core:v5.0.0-alpha.1-androidx")

    // XXPermissions for permission handling
    implementation("com.github.getActivity:XXPermissions:18.6")
}
