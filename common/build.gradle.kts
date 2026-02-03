import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
}

group = "de.proglove.example.common"
version = "1.0-SNAPSHOT"


kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
//            sourceCompatibility = Versions.Java.sourceCompatibility
//            targetCompatibility = Versions.Java.targetCompatibility
}
project.tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_3)
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

}