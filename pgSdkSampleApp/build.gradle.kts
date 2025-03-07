import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "de.proglove.example.sdk"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.proglove.example.sdk"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val keystorePropertiesFile = rootProject.file("./keystore.properties")
        val keystoreProperties = Properties()
        if (keystorePropertiesFile.exists() && keystorePropertiesFile.canRead()) {
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))
        }
        create("release") {

            if (keystoreProperties.getProperty("KEY_ALIAS") != null) {
                keyAlias = keystoreProperties.getProperty("KEY_ALIAS")
            }
            if (keystoreProperties.getProperty("KEY_PASSWORD") != null) {
                keyPassword = keystoreProperties.getProperty("KEY_PASSWORD")
            }
            if (keystoreProperties.getProperty("STORE_FILE") != null) {
                storeFile = rootProject.file(keystoreProperties.getProperty("STORE_FILE"))
            }
            if (keystoreProperties.getProperty("STORE_PASSWORD") != null) {
                storePassword = keystoreProperties.getProperty("STORE_PASSWORD")
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":common"))
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    // Integrating ProGlove SDK
    if (findProject(":connect-sdk") != null) {
        implementation(project(":connect-sdk"))
    } else {
        implementation(libs.proglove.connect.sdk)
    }
}