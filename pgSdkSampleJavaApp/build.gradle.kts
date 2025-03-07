plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "de.proglove.example.sdk.java"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.proglove.example.sdk.java"
        minSdk = 24
        targetSdk = 35
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":common"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    // Integrating ProGlove SDK
    if (findProject(":connect-sdk") != null) {
        implementation(project(":connect-sdk"))
    } else {
        implementation(libs.proglove.connect.sdk)
    }
}