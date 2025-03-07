// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

subprojects {
    configurations.configureEach {
        // Check for updates every sync
        //resolutionStrategy.cacheChangingModulesFor(0, "seconds")
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
        }
    }
}