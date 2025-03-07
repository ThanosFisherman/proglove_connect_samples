pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://dl.cloudsmith.io/public/proglove/pgconnect-public/maven/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://dl.cloudsmith.io/public/proglove/pgconnect-public/maven/") }
    }
}

rootProject.name = "proglove_connect_samples"
include(
    ":common",
    ":pgSlimIntentSampleJavaApp",
    ":pgSlimIntentSampleApp",
//    ":pgSdkSampleApp",
//    ":pgSdkSampleJavaApp",
//    ":pgIntentSampleApp"
)
