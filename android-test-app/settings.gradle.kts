pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://storage.googleapis.com/download.flutter.io")
    }
}

rootProject.name = "Android Test app"
include(":app")

// Flutter add-to-app module (source integration).
apply(from = File(settingsDir.parentFile, "integration_test_sample/.android/include_flutter.groovy"))

// Keep Dart integration_test dependency, but avoid evaluating the SDK Android plugin project.
project(":integration_test").projectDir = File(settingsDir, "integration_test_stub")
