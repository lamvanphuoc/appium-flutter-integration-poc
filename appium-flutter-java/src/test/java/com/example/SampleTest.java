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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.List;

class SampleTest {
    private AppiumDriver driver;
    private DriverManager driverManager;
    private boolean platformAndroid;

    @BeforeEach
    void setUp() throws IOException {
        TestConfig config = TestConfig.load();
        platformAndroid = config.platformAndroid();
        Path androidAppPath = config.androidAppPath();
        Path iosAppPath = config.iosAppPath();
        URL serverUrl = config.serverUrl();
        driverManager = new DriverManager(config.properties(), serverUrl, androidAppPath, iosAppPath);
    }

    @Test
    void should_be_able_to_swap_drivers() throws IOException {
        // Keep the app running when switching drivers to assert both native and flutter views in the same test
        driver = createNativeDriver(false);

        // Assert native views are visible in UiAutomator2 session
        WebDriverWait nativeWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        By nativeButton = XPathLocators.byAnyText("Open Flutter app");
        WebElement tapMeButton = nativeWait.until(d -> d.findElement(nativeButton));
        Assertions.assertTrue(tapMeButton.isDisplayed(), "'Open Flutter app' button should be visible in session.");
        tapMeButton.click();

        // Swap to Flutter Driver to assert flutter views
        driver.quit();
        driver = driverManager.createFlutterIntegrationDriver(true, platformAndroid);

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

    @Test
    void various_assertions_on_flutter_views() throws IOException {
        // Arrange
        driver = createNativeDriver(false);
        WebDriverWait nativeWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        By nativeButton = XPathLocators.byAnyText("Open Flutter app");
        try {
            WebElement tapMeButton = nativeWait.until(d -> d.findElement(nativeButton));
            Assertions.assertTrue(tapMeButton.isDisplayed(), "'Open Flutter app' button should be visible in session.");
            tapMeButton.click();
        } catch (TimeoutException ignored) {
            // noReset may keep app on Flutter screen from previous runs; continue with Flutter driver assertions.
        }

        driver.quit();
        driver = driverManager.createFlutterIntegrationDriver(true, platformAndroid);

        // Act and Assert
        WebDriverWait integrationWait = new WebDriverWait(driver, Duration.ofSeconds(20));


        WebElement textField = integrationWait.until(d -> d.findElement(FlutterBy.type("TextField")));
        textField.click();
        textField.sendKeys("hello!");

        WebElement newScreenButton = integrationWait.until(d -> d.findElement(FlutterBy.key("NewScreen")));
        Assertions.assertTrue(
                newScreenButton.isDisplayed(),
                "Expected button with key 'NewScreen' to be visible in FlutterIntegration session.");
        newScreenButton.click();

        WebElement itemTwenty = null;
        for (int i = 0; i < 30; i++) {
            try {
                itemTwenty = driver.findElement(FlutterBy.semanticsLabel("Item number 20"));
                if (itemTwenty.isDisplayed()) {
                    break;
                }
            } catch (WebDriverException ignored) {
                // Keep swiping until target item is visible.
            }
            swipeUp();
        }
        Assertions.assertNotNull(itemTwenty, "Expected list item with semantics label 'Item number 20' to be visible after scrolling.");
        Assertions.assertTrue(itemTwenty.isDisplayed(), "Expected list item with semantics label 'Item number 20' to be visible after scrolling.");
    }

    private void swipeUp() {
        Dimension size = driver.manage().window().getSize();
        int centerX = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.75);
        int endY = (int) (size.getHeight() * 0.30);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(400), PointerInput.Origin.viewport(), centerX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(List.of(swipe));
    }

    private AppiumDriver createNativeDriver(boolean closeAppWhenDone) throws IOException {
        if (platformAndroid) {
            return driverManager.createUiAutomator2Driver(closeAppWhenDone);
        }
        return driverManager.createXCUITestDriver(closeAppWhenDone);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
