package com.github.adinsa.picevolve;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;

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
}
