package com.example;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.options.BaseOptions;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

class DriverManager {
    private static final String UI_AUTOMATOR2 = "UiAutomator2";
    private static final String XCUITEST = "XCUITest";
    private static final String FLUTTER_INTEGRATION = "FlutterIntegration";
    private static final int FLUTTER_SYSTEM_PORT = 9000;
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_BASE_MS = 1000L;

    private final Properties config;
    private final URL serverUrl;
    private final Path androidAppPath;
    private final Path iosAppPath;

    DriverManager(Properties config, URL serverUrl, Path androidAppPath, Path iosAppPath) {
        this.config = config;
        this.serverUrl = serverUrl;
        this.androidAppPath = androidAppPath;
        this.iosAppPath = iosAppPath;
    }

    AppiumDriver createUiAutomator2Driver(boolean closeAppWhenDone) throws IOException {
        return createDriverWithRetry(UI_AUTOMATOR2, DEFAULT_MAX_ATTEMPTS, closeAppWhenDone, true);
    }

    AppiumDriver createXCUITestDriver(boolean closeAppWhenDone) throws IOException {
        return createDriverWithRetry(XCUITEST, DEFAULT_MAX_ATTEMPTS, closeAppWhenDone, false);
    }

    AppiumDriver createFlutterIntegrationDriver(boolean closeAppWhenDone, boolean platformAndroid) throws IOException {
        return createDriverWithRetry(FLUTTER_INTEGRATION, DEFAULT_MAX_ATTEMPTS, closeAppWhenDone, platformAndroid);
    }

    private AppiumDriver createDriverWithRetry(
            String automationName,
            int maxAttempts,
            boolean closeAppWhenDone,
            boolean platformAndroid) throws IOException {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return createDriver(automationName, closeAppWhenDone, platformAndroid);
            } catch (RuntimeException e) {
                lastError = e;
                if (attempt == maxAttempts) {
                    break;
                }
                sleepBeforeRetry(automationName, attempt);
            }
        }
        throw lastError;
    }

    private AppiumDriver createDriver(String automationName, boolean closeAppWhenDone, boolean platformAndroid)
            throws IOException {
        if (Objects.equals(automationName, FLUTTER_INTEGRATION)) {
            return new AppiumDriver(serverUrl, buildFlutterIntegrationDriverOptions(closeAppWhenDone, platformAndroid));
        } else if (Objects.equals(automationName, UI_AUTOMATOR2)) {
            return new AppiumDriver(serverUrl, buildAndroidUIAutomatorOptions(closeAppWhenDone));
        } else if (Objects.equals(automationName, XCUITEST)) {
            return new AppiumDriver(serverUrl, buildIosXCUITestOptions(closeAppWhenDone));
        }
        throw new IllegalArgumentException("Unsupported automation name: " + automationName);
    }

    private BaseOptions<?> buildAndroidUIAutomatorOptions(boolean closeAppWhenDone) throws IOException {
        BaseOptions<?> options = new BaseOptions<>()
                .setPlatformName("Android")
                .amend("appium:automationName", UI_AUTOMATOR2)
                .amend("appium:deviceName", required("android.deviceName"))
                .amend("appium:noReset", true)
                .amend("appium:dontStopAppOnReset", true)
                .amend("appium:shouldTerminateApp", closeAppWhenDone)
                .amend("appium:forceAppLaunch", false)
                .amend("appium:flutterSystemPort", FLUTTER_SYSTEM_PORT)
                .amend("appium:flutterServerLaunchTimeout", 60000)
                .amend("appium:app", androidAppPath.toString());

        String platformVersion = trimToNull(config.getProperty("android.platformVersion"));
        if (platformVersion != null) {
            options.amend("appium:platformVersion", platformVersion);
        }
        return options;
    }

    private BaseOptions<?> buildIosXCUITestOptions(boolean closeAppWhenDone) throws IOException {
        BaseOptions<?> options = new BaseOptions<>()
                .setPlatformName("iOS")
                .amend("appium:automationName", XCUITEST)
                .amend("appium:deviceName", required("ios.deviceName"))
                .amend("appium:noReset", true)
                .amend("appium:shouldTerminateApp", closeAppWhenDone)
                .amend("appium:forceAppLaunch", false)
                .amend("appium:app", iosAppPath.toString());

        String platformVersion = trimToNull(config.getProperty("ios.platformVersion"));
        if (platformVersion != null) {
            options.amend("appium:platformVersion", platformVersion);
        }
        return options;
    }

    private BaseOptions<?> buildFlutterIntegrationDriverOptions(boolean closeSessionWhenDone, boolean platformAndroid)
            throws IOException {
        String platformName = platformAndroid ? "Android" : "iOS";
        String deviceName = platformAndroid ? required("android.deviceName") : required("ios.deviceName");
        Path appPath = platformAndroid ? androidAppPath : iosAppPath;
        String platformVersionKey = platformAndroid ? "android.platformVersion" : "ios.platformVersion";

        BaseOptions<?> options = new BaseOptions<>()
                .setPlatformName(platformName)
                .amend("appium:automationName", FLUTTER_INTEGRATION)
                .amend("appium:deviceName", deviceName)
                .amend("appium:noReset", true)
                .amend("appium:dontStopAppOnReset", true)
                .amend("appium:shouldTerminateApp", closeSessionWhenDone)
                .amend("appium:forceAppLaunch", false)
                .amend("appium:flutterServerLaunchTimeout", 60000)
                .amend("appium:app", appPath.toString());

        if (platformAndroid) {
            options.amend("appium:flutterSystemPort", FLUTTER_SYSTEM_PORT);
        }

        String platformVersion = trimToNull(config.getProperty(platformVersionKey));
        if (platformVersion != null) {
            options.amend("appium:platformVersion", platformVersion);
        }
        return options;
    }

    private void sleepBeforeRetry(String automationName, int attempt) {
        try {
            Thread.sleep(RETRY_DELAY_BASE_MS * attempt);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Interrupted while retrying " + automationName + " session startup.",
                    interruptedException);
        }
    }

    private String required(String key) throws IOException {
        String value = trimToNull(config.getProperty(key));
        if (value == null) {
            throw new IOException("Missing required config value: " + key);
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return Objects.equals(trimmed, "") ? null : trimmed;
    }
}
