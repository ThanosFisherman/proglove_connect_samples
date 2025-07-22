plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.pgsdksamplejavaapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pgsdksamplejavaapp"
        minSdk = 24
        targetSdk = 35
        versionCode = System.getenv("VERSION_CODE")?.toInt() ?: 1
        versionName = "1.0"
        buildConfigField("int", "VERSION_CODE", versionCode.toString())
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
        debug {
            isMinifyEnabled = false
            isDefault = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
           // buildConfigField("String", "VERSION_CODE", defaultConfig.versionCode.toString())
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":common"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    // Integrating ProGlove SDK
    if (findProject(":connect-sdk") != null) {
        implementation(project(":connect-sdk"))
    } else {
        implementation(libs.proglove.connect.sdk)
    }
}