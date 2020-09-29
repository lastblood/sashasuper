package ru.sashasuper.io;

import ru.sashasuper.logic.Vector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import static ru.sashasuper.utils.Assertions.thr;

public class ImageReader extends DataReader {
    private String propertiesFilePath;
    private boolean ignoreWrongFiles = false;
    private boolean inverse = false;

    private int scaleToWidth = 0;
    private int scaleToHeight = 0;

    private Function<Vector, Vector> keyMapper = null;
    private Function<Vector, Vector> valueMapper = null;

    public ImageReader(String propertiesFilePath) {
        this.propertiesFilePath = propertiesFilePath;
    }

    public ImageReader(String propertiesFilePath, boolean ignoreWrongFiles) {
        this.propertiesFilePath = propertiesFilePath;
        this.ignoreWrongFiles = ignoreWrongFiles;
    }

    public BufferedImage readImage(String imageFilePath) throws IOException {
        File f = new File(imageFilePath);

        if(!f.exists() || !f.canRead())
            throw new FileNotFoundException("Doesn't exists: " + imageFilePath);

        return ImageIO.read(f);
    }

    // Запись построчная. Значения [0;255]. Цветные изображения обесцвечиваются
    private Vector vectorFromImage(BufferedImage image) {
        int h = image.getHeight(), w = image.getWidth();
        float[] vectorArray = new float[h * w];

        int vectorIndex = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++, vectorIndex++) {
                int pixel = image.getRGB(x, y);
                vectorArray[vectorIndex] = (((pixel >> 16) & 0xFF) + ((pixel >> 8) & 0xFF) + (pixel & 0xFF)) / 3.0f;
                if(inverse) vectorArray[vectorIndex] = 255f - vectorArray[vectorIndex];
            }
        }

        return new Vector(false, vectorArray);
    }

    // Из значения properties вектор может быть прочитан двумя способами:
    // - position-value: для классификации (просто значение)
    // - number-list: универсальный [1, 2, 3]
    private Vector readValueVector(String propertyValue) throws IOException {
        try {
            return new Vector(Float.parseFloat(propertyValue.strip()));
        } catch(NumberFormatException e) {
            try {
                String[] splitted = propertyValue
                        .replaceFirst("\\[", "")
                        .replaceFirst("]", "")
                        .split(",\\s?");

                float[] array = new float[splitted.length];
                for (int i = 0; i < splitted.length; i++)
                    array[i] = Float.parseFloat(splitted[i]);

                return new Vector(array);
            } catch (NumberFormatException nfe) {
                throw new IOException("Can't read propertyValue: " + propertyValue);
            }
        }
    }

    @Override
    public Dataset read() throws IOException {
        Properties properties = new Properties();
        File propertiesFile = new File(propertiesFilePath);

        if(!propertiesFile.isFile() || !propertiesFile.canRead())
            throw new IOException("Can't read properties file at " + propertiesFilePath);

        try (InputStream propertiesLoader = new FileInputStream(propertiesFile)) {
            properties.load(propertiesLoader);
        }

        List<SimpleEntry<Vector, Vector>> result = new ArrayList<>();

        String path = propertiesFile.getParent() + File.separator;
        for (String propertyName : properties.stringPropertyNames()) {
            try {
                BufferedImage image = readImage(path + propertyName);

                // Изменяет размер при необходимости
                if(scaleToHeight > 0 && scaleToWidth > 0)
                    image = rescale(image);

                Vector key = vectorFromImage(image);
                Vector value = readValueVector(properties.getProperty(propertyName));

                if(keyMapper != null) key = keyMapper.apply(key);
                if(valueMapper != null) value = valueMapper.apply(value);

                result.add(new SimpleEntry<>(key, value));
            } catch (IOException ioe) {
                if(ignoreWrongFiles) // Ругается на неправильные файлы или записи, но не вылетает
                    System.out.println(ioe.toString());
                else
                    throw ioe;
            }
        }

        return new SimpleDataset(result);
    }

    private BufferedImage rescale(BufferedImage image) {
        if(image.getHeight() != scaleToHeight && image.getWidth() != scaleToWidth) {
            Image scaled = image.getScaledInstance(scaleToWidth, scaleToHeight, Image.SCALE_DEFAULT);

            thr(scaled.getWidth(null) != scaleToWidth || scaled.getHeight(null) != scaleToHeight,
                    "Failed image rescaling");

            BufferedImage buffered = new BufferedImage(scaleToWidth, scaleToHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics g = buffered.getGraphics();
            g.drawImage(scaled, 0, 0, null);
            g.dispose();

            image = buffered;
        }
        return image;
    }

    public void setScale(int scaleToWidth, int scaleToHeight) {
        thr(scaleToHeight<2 || scaleToWidth<2);
        this.scaleToWidth = scaleToWidth;
        this.scaleToHeight = scaleToHeight;
    }

    public void setKeyMapper(Function<Vector, Vector> keyMapper) {
        this.keyMapper = keyMapper;
    }

    public void setValueMapper(Function<Vector, Vector> valueMapper) {
        this.valueMapper = valueMapper;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }
}
