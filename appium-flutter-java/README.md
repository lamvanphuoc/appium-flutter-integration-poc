# Appium Flutter Integration (Java + Gradle)

This project is a minimal Android smoke test that verifies Flutter UI elements are visible using Appium Flutter Integration Driver.

## Prerequisites

- Java 17+
- Appium 2.x installed and running
- Android emulator or device available
- Flutter app built with `appium_flutter_server` integration and APK ready

Install the driver:

```bash
appium driver install --source npm appium-flutter-integration-driver
```

## Project Structure

- `build.gradle`: dependencies and JUnit config
- `src/test/java/com/example/HelloButtonVisibleTest.java`: the smoke test
- `src/test/resources/config.properties`: runtime config values

The test starts with native locator checks in `UiAutomator2`, then uses Flutter finder strategy (`FlutterBy.text("Integration Test")`) after switching to `FlutterIntegration`.

## Configure Runtime Values

Edit `src/test/resources/config.properties`:

- `appium.server.url`: Appium server endpoint (default `http://127.0.0.1:4723`)
- `android.deviceName`: emulator/device name
- `android.platformVersion`: optional Android version
- `app.path`: path to your Flutter APK

## Run the Test

```bash
./gradlew test --tests com.example.SampleTest
```

## Troubleshooting

- If `app.path` still points to the placeholder value, the test is skipped by design. Set it to a real APK path.
- If `"Integration Test"` is not found in the Flutter session, verify your app title and Flutter integration setup, then adjust the Flutter finder text in `SampleTest`.
