package com.example;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.AppiumBy;
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
import org.devicefarm.FlutterBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

class SampleTest {
    private static final String CONFIG_FILE = "config.properties";

    private AppiumDriver driver;
    private DriverManager driverManager;
    private Properties config;

    @BeforeEach
    void setUp() throws IOException {
        config = loadConfig();
        Path appPath = Path.of(required("app.path")).toAbsolutePath();
        Assumptions.assumeTrue(Files.exists(appPath), "Set app.path to an existing APK before running this test.");
        URL serverUrl = URI.create(required("appium.server.url")).toURL();
        driverManager = new DriverManager(config, serverUrl, appPath);

        driver = driverManager.createUiAutomator2Driver();
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
        driver = driverManager.createFlutterIntegrationDriver();

        WebDriverWait integrationWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        By integrationTitleLocator = FlutterBy.text("Integration Test");
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

}
