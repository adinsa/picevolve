package com.github.adinsa.picevolve.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads 'application.properties' file and extract application-specific properties.
 *
 * @author amar
 *
 */
class Configuration {

    private static final String CONFIGURATION_FILE = "/application.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private final Properties properties = new Properties();

    Configuration() throws IOException {
        try (final InputStream in = getClass().getResourceAsStream(CONFIGURATION_FILE)) {
            if (in == null) {
                throw new IOException("Configuration file not found on classpath: " + CONFIGURATION_FILE);
            }
            properties.load(in);
        }
        LOGGER.debug("Loaded properties from {}: {}", CONFIGURATION_FILE, properties);
    }

    public String getImagesDirectory() {
        return properties.getProperty("images.dir");
    }

    public String getLibraryFile() {
        return properties.getProperty("library.file");
    }

    public int getPreviewWidth() {
        return Integer.parseInt(properties.getProperty("preview.width"));
    }

    public int getPreviewHeight() {
        return Integer.parseInt(properties.getProperty("preview.height"));
    }

    public String getImageFormat() {
        return properties.getProperty("image.format");
    }
}