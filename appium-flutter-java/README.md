# Appium Flutter Integration (Java + Gradle)

This project is a smoke test that verifies Flutter UI elements are visible using Appium Flutter Integration Driver on Android or iOS.
https://github.com/AppiumTestDistribution/appium-flutter-integration-driver
Locator helper: https://github.com/AppiumTestDistribution/flutter-finder 

## Prerequisites

- Java 17+
- Appium 2.x installed and running
- Android emulator/device or iOS simulator available
- Flutter app built with `appium_flutter_server` integration and app artifacts ready

Install the driver:

```bash
appium driver install --source npm appium-flutter-integration-driver
```

## Appium Versions

Use these versions so setup matches the CartonCloud project

- Appium: `2.0.0-beta.35`
- Drivers:
  - `uiautomator2@2.34.2`
  - `xcuitest@7.35.0`
  - `flutter-integration@1.1.3`

## Project Structure

- `build.gradle`: dependencies and JUnit config
- `src/test/java/com/example/SampleTest.java`: the smoke test
- `src/test/resources/config.properties`: runtime config values

The test starts with native locator checks in `UiAutomator2` (Android) or `XCUITest` (iOS), then uses Flutter finder strategy after switching to `FlutterIntegration`.
`FlutterBy` is consumed directly from the `flutter-finder` library dependency declared in `build.gradle` (via JitPack), not vendored source.

## Configure Runtime Values

Edit `src/test/resources/config.properties`:

- `appium.server.url`: Appium server endpoint (default `http://127.0.0.1:4723`)
- `platform.android`: set `true` for Android (`UiAutomator2`) or `false` for iOS (`XCUITest`)
- `android.deviceName`: emulator/device name
- `android.platformVersion`: optional Android version
- `ios.deviceName`: iOS simulator name
- `ios.platformVersion`: optional iOS version
- `app.android.path`: path to your Flutter APK
- `app.ios.path`: path to your iOS `.app` bundle

## Generate App Build Artifacts

### Android APK

Run the APK build command:

```bash
cd android-test-app
./gradlew :app:assembleDebug -Ptarget=/Users/lamvan/Projects/mobile-cartoncloud/appium-flutter-integration-poc/flutter-test-app/integration_test/app_test.dart
```

### iOS Simulator app bundle (`ios-test-app.app`)

Run the iOS simulator build command from `ios-test-app`:

```bash
# 1. Update the Flutter build configuration for the specific test target
cd flutter-test-app
flutter build ios --config-only integration_test/app_test.dart

# 2. Build the app using xcodebuild with the target variable
cd ios-test-app
xcodebuild -workspace ios-test-app.xcworkspace \
           -scheme ios-test-app \
           -configuration Debug \
           -sdk iphonesimulator \
           -derivedDataPath build \
           FLUTTER_TARGET=/Users/lamvan/Projects/mobile-cartoncloud/appium-flutter-integration-poc/flutter-test-app/integration_test/app_test.dart
```

## Run the Test

```bash
./gradlew test --tests com.example.SampleTest
```

## Troubleshooting

- If `app.android.path` or `app.ios.path` points to a placeholder/nonexistent file, the test is skipped by design. Set both to real artifacts.
- If `"Integration Test"` is not found in the Flutter session, verify your app title and Flutter integration setup, then adjust the Flutter finder text in `SampleTest`.
