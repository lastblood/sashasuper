package ru.sashasuper.io;

import ru.sashasuper.logic.Vector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageReader {
    private ImageReader() {
    }

    public static BufferedImage readImage(String fileName) throws IOException {
        File f = new File(fileName);
        if(!f.exists() || !f.canRead())
            throw new FileNotFoundException("Doesn't exists: " + fileName);

        return ImageIO.read(f);
    }

    // Запись построчная. Значения [0;255]. Цветные изображения обесцвечиваются
    public static Vector vectorFromImage(BufferedImage image) {
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

