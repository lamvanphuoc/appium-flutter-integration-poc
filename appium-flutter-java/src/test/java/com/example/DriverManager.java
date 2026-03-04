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
    private static final String FLUTTER_INTEGRATION = "FlutterIntegration";
    private static final int FLUTTER_SYSTEM_PORT = 9000;
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_BASE_MS = 1000L;

    private final Properties config;
    private final URL serverUrl;
    private final Path appPath;

    DriverManager(Properties config, URL serverUrl, Path appPath) {
        this.config = config;
        this.serverUrl = serverUrl;
        this.appPath = appPath;
    }

    AppiumDriver createUiAutomator2Driver(boolean closeAppWhenDone) throws IOException {
        return createDriverWithRetry(UI_AUTOMATOR2, DEFAULT_MAX_ATTEMPTS, closeAppWhenDone);
    }

    AppiumDriver createFlutterIntegrationDriver(boolean closeAppWhenDone) throws IOException {
        return createDriverWithRetry(FLUTTER_INTEGRATION, DEFAULT_MAX_ATTEMPTS, closeAppWhenDone);
    }

    private AppiumDriver createDriverWithRetry(String automationName, int maxAttempts, boolean closeAppWhenDone)
            throws IOException {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return createDriver(automationName, closeAppWhenDone);
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

    private AppiumDriver createDriver(String automationName, boolean closeAppWhenDone) throws IOException {
        if(Objects.equals(automationName, FLUTTER_INTEGRATION)) {
            return new AppiumDriver(serverUrl, buildFlutterIntegrationDriverOptions(closeAppWhenDone));
        } else if(Objects.equals(automationName, UI_AUTOMATOR2)) {
            return new AppiumDriver(serverUrl, buildAndroidUIAutomatorOptions(closeAppWhenDone));
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
                .amend("appium:app", appPath.toString());

        String platformVersion = trimToNull(config.getProperty("android.platformVersion"));
        if (platformVersion != null) {
            options.amend("appium:platformVersion", platformVersion);
        }
        return options;
    }

    private BaseOptions<?> buildFlutterIntegrationDriverOptions(boolean closeSessionWhenDone) throws IOException {
        BaseOptions<?> options = new BaseOptions<>()
                .setPlatformName("Android")
                .amend("appium:automationName", FLUTTER_INTEGRATION)
                .amend("appium:deviceName", required("android.deviceName"))
                .amend("appium:noReset", true)
                .amend("appium:dontStopAppOnReset", true)
                .amend("appium:shouldTerminateApp", closeSessionWhenDone)
                .amend("appium:forceAppLaunch", false)
                .amend("appium:flutterSystemPort", FLUTTER_SYSTEM_PORT)
                .amend("appium:flutterServerLaunchTimeout", 60000)
                .amend("appium:app", appPath.toString());

        String platformVersion = trimToNull(config.getProperty("android.platformVersion"));
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
