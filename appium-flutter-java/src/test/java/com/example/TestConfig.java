package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import org.junit.jupiter.api.Assumptions;

class TestConfig {
    private static final String CONFIG_FILE = "config.properties";

    private final Properties properties;

    private TestConfig(Properties properties) {
        this.properties = properties;
    }

    static TestConfig load() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IOException("Missing " + CONFIG_FILE + " in src/test/resources");
            }
            properties.load(inputStream);
        }
        return new TestConfig(properties);
    }

    Properties properties() {
        return properties;
    }

    boolean platformAndroid() throws IOException {
        String value = required("platform.android").toLowerCase();
        if (Objects.equals(value, "true")) {
            return true;
        }
        if (Objects.equals(value, "false")) {
            return false;
        }
        throw new IOException("Invalid boolean value for platform.android: " + value);
    }

    Path androidAppPath() throws IOException {
        return requiredAppPath("app.android.path", "Set app.android.path to an existing APK before running this test.");
    }

    Path iosAppPath() throws IOException {
        return requiredAppPath("app.ios.path", "Set app.ios.path to an existing iOS .app bundle before running this test.");
    }

    URL serverUrl() throws IOException {
        return URI.create(required("appium.server.url")).toURL();
    }

    private Path requiredAppPath(String key, String missingPathMessage) throws IOException {
        Path appPath = Path.of(required(key)).toAbsolutePath();
        Assumptions.assumeTrue(Files.exists(appPath), missingPathMessage);
        return appPath;
    }

    private String required(String key) throws IOException {
        String value = trimToNull(properties.getProperty(key));
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
