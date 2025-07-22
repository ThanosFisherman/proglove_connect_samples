import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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
        versionCode = System.getenv("VERSION_CODE")?.toInt() ?: 1
        versionName = "1.0"
        buildConfigField("int", "VERSION_CODE", versionCode.toString())
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
        debug {
            isMinifyEnabled = false
            isDefault = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("int", "VERSION_CODE", defaultConfig.versionCode.toString())
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
        compilerOptions {
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
            jvmTarget.set(JvmTarget.fromTarget("21"))
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
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