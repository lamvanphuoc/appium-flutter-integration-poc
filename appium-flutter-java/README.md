# Appium Flutter Integration (Java + Gradle)

This project is a minimal Android smoke test that verifies Flutter UI elements are visible using Appium Flutter Integration Driver.
https://github.com/AppiumTestDistribution/appium-flutter-integration-driver
Locator helper: https://github.com/AppiumTestDistribution/flutter-finder 

## Prerequisites

- Java 17+
- Appium 2.x installed and running
- Android emulator or device available
- Flutter app built with `appium_flutter_server` integration and APK ready

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

The test starts with native locator checks in `UiAutomator2`, then uses Flutter finder strategy (`FlutterBy.text("Integration Test")`) after switching to `FlutterIntegration`.
`FlutterBy` is consumed directly from the `flutter-finder` library dependency declared in `build.gradle` (via JitPack), not vendored source.

## Configure Runtime Values

Edit `src/test/resources/config.properties`:

- `appium.server.url`: Appium server endpoint (default `http://127.0.0.1:4723`)
- `android.deviceName`: emulator/device name
- `android.platformVersion`: optional Android version
- `app.path`: path to your Flutter APK or iOS `.app` bundle

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

- If `app.path` still points to the placeholder value, the test is skipped by design. Set it to a real APK path.
- If `"Integration Test"` is not found in the Flutter session, verify your app title and Flutter integration setup, then adjust the Flutter finder text in `SampleTest`.
