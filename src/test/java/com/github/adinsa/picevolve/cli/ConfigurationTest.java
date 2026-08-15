package com.github.adinsa.picevolve.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ConfigurationTest {

    @Test
    public void testLoadsProperties() throws IOException {

        final Configuration configuration = new Configuration();

        assertEquals("images", configuration.getImagesDirectory());
        assertEquals(".library.dat", configuration.getLibraryFile());
        assertEquals(200, configuration.getPreviewWidth());
        assertEquals(200, configuration.getPreviewHeight());
        assertEquals("png", configuration.getImageFormat());
    }
}
