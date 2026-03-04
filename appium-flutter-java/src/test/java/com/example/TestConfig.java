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

    Path appPath() throws IOException {
        Path appPath = Path.of(required("app.path")).toAbsolutePath();
        Assumptions.assumeTrue(Files.exists(appPath), "Set app.path to an existing APK before running this test.");
        return appPath;
    }

    URL serverUrl() throws IOException {
        return URI.create(required("appium.server.url")).toURL();
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
