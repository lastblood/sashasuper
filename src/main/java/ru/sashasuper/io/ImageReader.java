package ru.sashasuper.io;

import ru.sashasuper.logic.Vector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageReader {
    private final String imageFileName;
    public ImageReader(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public BufferedImage readImage() throws IOException {
        File f = new File(imageFileName);

        if(!f.exists() || !f.canRead())
            throw new FileNotFoundException("Doesn't exists: " + imageFileName);

        return ImageIO.read(f);
    }

    // Запись построчная. Значения [0;255]. Цветные изображения обесцвечиваются
    private Vector vectorFromImage(BufferedImage image) {
        int h = image.getHeight(), w = image.getWidth();
        float[] vectorArray = new float[h * w];
        Vector v = new Vector(vectorArray.length, vectorArray);

        int vectorIndex = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = image.getRGB(x, y);
                vectorArray[vectorIndex++] = (((pixel >> 16) & 0xFF) + ((pixel >> 8) & 0xFF) + (pixel & 0xFF)) / 3.0f;
            }
        }

        return v;
    }
}

