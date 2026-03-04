package com.example;

import io.appium.java_client.AppiumDriver;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.devicefarm.FlutterBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

class SampleTest {
    private AppiumDriver driver;
    private DriverManager driverManager;

    @BeforeEach
    void setUp() throws IOException {
        TestConfig config = TestConfig.load();
        Path appPath = config.appPath();
        URL serverUrl = config.serverUrl();
        driverManager = new DriverManager(config.properties(), serverUrl, appPath);
    }

    @Test
    void should_be_able_to_swap_drivers() throws IOException {
        // Keep the app running when switching drivers to assert both native and flutter views in the same test
        driver = driverManager.createUiAutomator2Driver(false);

        // Assert native views are visible in UiAutomator2 session
        WebDriverWait nativeWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        By nativeButton = XPathLocators.byAnyText("Open Flutter app");
        WebElement tapMeButton = nativeWait.until(d -> d.findElement(nativeButton));
        Assertions.assertTrue(tapMeButton.isDisplayed(), "'Open Flutter app' button should be visible in UiAutomator2 session.");
        tapMeButton.click();

        // Swap to Flutter Driver to assert flutter views
        driver.quit();
        driver = driverManager.createFlutterIntegrationDriver(true);

        WebDriverWait integrationWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        By appBarLocator = FlutterBy.type("AppBar");
        WebElement appBar = integrationWait.until(d -> d.findElement(appBarLocator));
        Assertions.assertTrue(
                appBar.isDisplayed(),
                "Expected AppBar to be visible after switching to FlutterIntegration driver.");
        WebElement integrationTitle = integrationWait.until(d -> appBar.findElement(FlutterBy.text("Flutter Test App")));
        Assertions.assertTrue(
                integrationTitle.isDisplayed(),
                "Expected AppBar to contain title text 'Flutter Test App' after switching to FlutterIntegration driver.");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
