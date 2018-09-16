package com.github.adinsa.picevolve.cli;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads 'application.properties' file and extract application-specific properties.
 *
 * @author amar
 *
 */
class Configuration extends Properties {

    private static final long serialVersionUID = -4595520619448477330L;

    private static final String CONFIGURATION_FILE = "/application.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private final Properties delegate = new Properties();

    Configuration() throws IOException {
        delegate.load(this.getClass().getResourceAsStream(CONFIGURATION_FILE));
        LOGGER.debug("Loaded properties from {}: {}", CONFIGURATION_FILE, delegate);
    }

    public String getImagesDirectory() {
        return delegate.getProperty("images.dir");
    }

    public String getLibraryFile() {
        return delegate.getProperty("library.file");
    }

    public int getPreviewWidth() {
        return Integer.parseInt(delegate.getProperty("preview.width"));
    }

    public int getPreviewHeight() {
        return Integer.parseInt(delegate.getProperty("preview.height"));
    }

    public String getImageFormat() {
        return delegate.getProperty("image.format");
    }
}