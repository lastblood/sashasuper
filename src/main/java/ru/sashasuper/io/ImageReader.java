package ru.sashasuper.io;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageReader {
    public BufferedImage readImage(String fileName) throws IOException {
        BufferedImage image = null;

        File f = new File(fileName);
        image = ImageIO.read(f);

        return image;
    }
}
