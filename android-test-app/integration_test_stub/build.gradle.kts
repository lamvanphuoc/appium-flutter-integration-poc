plugins {
    id("com.android.library")
}

val flutterSdkPath = "/Users/lamvan/fvm/versions/3.32.5"
val integrationTestAndroidDir = "$flutterSdkPath/packages/integration_test/android"

android {
    namespace = "dev.flutter.plugins.integration_test"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        getByName("main") {
            java.srcDir("$integrationTestAndroidDir/src/main/java")
        }
    }
}

dependencies {
    implementation("com.google.guava:guava:28.1-android")
    api("androidx.test:runner:1.2+")
    api("androidx.test:rules:1.2+")
    api("androidx.test.espresso:espresso-core:3.2+")
}
