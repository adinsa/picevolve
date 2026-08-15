package com.github.adinsa.picevolve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.jupiter.api.Test;

public class ImageTest {

    @Test
    public void fromBufferedImagePreservesAllRgbChannels() {

        final BufferedImage buf = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        buf.setRGB(0, 0, new Color(100, 150, 200).getRGB());

        final Image image = Image.fromBufferedImage(buf);

        final Image.Pixel pixel = image.get(0, 0);

        assertEquals(100, pixel.r(), 0);
        assertEquals(150, pixel.g(), 0);
        assertEquals(200, pixel.b(), 0);
    }

    @Test
    public void testWriteToInvalidPathThrowsWithCause() {

        final Image image = new Image(1, 1);

        final RuntimeException ex = assertThrows(RuntimeException.class,
                () -> image.write(new File("/nonexistent-dir/foo.png"), "png"));

        assertNotNull(ex.getCause(), "Write failure should preserve the underlying cause");
    }

    @Test
    public void testAsBufferedImageSetsPixels() {

        final Image image = new Image(2, 1);
        image.set(0, 0, new Image.Pixel(1.0, 0.0, 0.5));
        image.set(1, 0, new Image.Pixel(0.0, 0.5, 1.0));

        final BufferedImage buf = image.asBufferedImage();

        assertEquals(2, buf.getWidth());
        assertEquals(1, buf.getHeight());

        final Color first = new Color(buf.getRGB(0, 0));
        assertEquals(255, first.getRed());
        assertEquals(0, first.getGreen());
        assertEquals(128, first.getBlue());

        final Color second = new Color(buf.getRGB(1, 0));
        assertEquals(0, second.getRed());
        assertEquals(128, second.getGreen());
        assertEquals(255, second.getBlue());
    }

    @Test
    public void testScaledNormalizesBetweenBounds() {

        final Image image = new Image(1, 2);
        image.set(0, 0, new Image.Pixel(0.0, 10.0, 100.0));
        image.set(0, 1, new Image.Pixel(50.0, 50.0, 50.0));

        final Image scaled = image.scaled(0, 1);

        assertEquals(0.0, scaled.get(0, 0).r(), 1e-9);
        assertEquals(0.1, scaled.get(0, 0).g(), 1e-9);
        assertEquals(1.0, scaled.get(0, 0).b(), 1e-9);
        assertEquals(0.5, scaled.get(0, 1).r(), 1e-9);
    }
}
