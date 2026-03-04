package com.example;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.remote.options.BaseOptions;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

class SampleTest {
    private static final String CONFIG_FILE = "config.properties";
    private static final String UI_AUTOMATOR2 = "UiAutomator2";
    private static final String FLUTTER_INTEGRATION = "FlutterIntegration";
    private static final int FLUTTER_SYSTEM_PORT = 9000;

    private AppiumDriver driver;
    private Properties config;
    private URL serverUrl;
    private Path appPath;

    @BeforeEach
    void setUp() throws IOException {
        config = loadConfig();
        appPath = Path.of(required("app.path")).toAbsolutePath();
        Assumptions.assumeTrue(Files.exists(appPath), "Set app.path to an existing APK before running this test.");
        serverUrl = URI.create(required("appium.server.url")).toURL();

        driver = createDriver(UI_AUTOMATOR2, true);
    }

    @Test
    void shouldSwitchFromUiAutomatorToFlutterIntegrationDriver() throws IOException {
        WebDriverWait nativeWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        By tapMeLocator = AppiumBy.xpath(
                "//*[(@text='Tap me' or @label='Tap me' or @name='Tap me' or normalize-space(.)='Tap me')]");
        WebElement tapMeButton = nativeWait.until(d -> d.findElement(tapMeLocator));
        Assertions.assertTrue(tapMeButton.isDisplayed(), "'Tap me' button should be visible in UiAutomator2 session.");
        tapMeButton.click();

        driver.quit();
        driver = createDriverWithRetry(FLUTTER_INTEGRATION, true, 3);

        WebDriverWait integrationWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        By integrationTitleLocator = AppiumBy.xpath(
                "//*[(@text='Integration Test' or @label='Integration Test' or @name='Integration Test' or normalize-space(.)='Integration Test')]");
        WebElement integrationTitle = integrationWait.until(d -> d.findElement(integrationTitleLocator));
        Assertions.assertTrue(
                integrationTitle.isDisplayed(),
                "Expected 'Integration Test' title to be visible after switching to FlutterIntegration driver.");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private Properties loadConfig() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IOException("Missing " + CONFIG_FILE + " in src/test/resources");
            }
            properties.load(inputStream);
        }
        return properties;
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

    private BaseOptions<?> androidOptions(String automationName, boolean includeApp) throws IOException {
        BaseOptions<?> options = new BaseOptions<>()
                .setPlatformName("Android")
                .amend("appium:automationName", automationName)
                .amend("appium:deviceName", required("android.deviceName"))
                .amend("appium:noReset", true)
                .amend("appium:dontStopAppOnReset", true)
                .amend("appium:shouldTerminateApp", false)
                .amend("appium:forceAppLaunch", false)
                .amend("appium:flutterSystemPort", FLUTTER_SYSTEM_PORT)
                .amend("appium:flutterServerLaunchTimeout", 60000);

        if (includeApp) {
            options.amend("appium:app", appPath.toString());
        }

        String platformVersion = trimToNull(config.getProperty("android.platformVersion"));
        if (platformVersion != null) {
            options.amend("appium:platformVersion", platformVersion);
        }
        return options;
    }

    private AppiumDriver createDriver(String automationName, boolean includeApp) throws IOException {
        return new AppiumDriver(serverUrl, androidOptions(automationName, includeApp));
    }

    private AppiumDriver createDriverWithRetry(String automationName, boolean includeApp, int maxAttempts)
            throws IOException {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return createDriver(automationName, includeApp);
            } catch (RuntimeException e) {
                lastError = e;
                if (attempt == maxAttempts) {
                    break;
                }
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while retrying FlutterIntegration session startup.",
                            interruptedException);
                }
            }
        }
        throw lastError == null ? new RuntimeException("Could not create driver session.") : lastError;
    }
}
