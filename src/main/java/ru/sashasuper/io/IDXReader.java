package ru.sashasuper.io;

import ru.sashasuper.logic.Vector;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import static ru.sashasuper.utils.Assertions.thr;

public class IDXReader {
    private String imagesFilePath;
    private String labelsFilePath;
    private boolean gzippedImages;
    private boolean gzippedLabels;

    public IDXReader(String imagesFilePath, String labelsFilePath, boolean gzipped) {
        this.imagesFilePath = imagesFilePath;
        this.labelsFilePath = labelsFilePath;
        this.gzippedLabels = gzipped;
        this.gzippedImages = gzipped;
    }

    public IDXReader(String imagesFilePath, String labelsFilePath, boolean gzippedImages, boolean gzippedLabels) {
        this.imagesFilePath = imagesFilePath;
        this.labelsFilePath = labelsFilePath;
        this.gzippedImages = gzippedImages;
        this.gzippedLabels = gzippedLabels;
    }

    private int readUnsignedInt(InputStream inImage) throws IOException {
        long temp = (inImage.read() << 24) | (inImage.read() << 16) | (inImage.read() << 8) | (inImage.read());
        thr(temp < 0, "Wrong value for unsigned integer: " + temp);
        return (int) temp;
    }

    // Сразу переводит в негатив, где 255 - белый
    private Vector readImageInVector(InputStream inImage, int length) throws IOException {
        float[] array = new float[length];

        for (int pixel = 0; pixel < length; pixel++)
            array[pixel] = 255 - inImage.read();

        return new Vector(length, array);
    }

    private HashMap<Integer, ArrayList<Vector>> readIDX
            (InputStream imageInput, InputStream labelInput, int pixels, int images) throws IOException {

        HashMap<Integer, ArrayList<Vector>> result = new HashMap<>();
        for(int i = 0; i < images; i++) {
            Vector vector = readImageInVector(imageInput, pixels);
            int label = labelInput.read();

            thr(label < 0, "Unexpected end of file");

            if(!result.containsKey(label))
                result.put(label, new ArrayList<>());

            result.get(label).add(vector);
        }
        return result;
    }

    public HashMap<Integer, ArrayList<Vector>> read() throws IOException {
        FileInputStream fisImages = new FileInputStream(imagesFilePath);
        FileInputStream fisLabels = new FileInputStream(labelsFilePath);

        try(InputStream imageInput = gzippedImages ? new GZIPInputStream(fisImages) : new BufferedInputStream(fisImages);
            InputStream labelInput = gzippedLabels ? new GZIPInputStream(fisLabels) : new BufferedInputStream(fisLabels)) {

            int magicNumberImages = readUnsignedInt(imageInput);
            thr(magicNumberImages != 2051, "Corrupted images file");
            int magicNumberLabels = readUnsignedInt(labelInput);
            thr(magicNumberLabels != 2049, "Corrupted labels file");

            int imagesCount = readUnsignedInt(imageInput);
            int labelsCount = readUnsignedInt(labelInput);
            thr(labelsCount != imagesCount, "Different labels and images files");

            int rows = readUnsignedInt(imageInput);
            int columns = readUnsignedInt(imageInput);
            thr(rows * columns <= 0, "Wrong rows (" + rows + ") or columns (" + columns + ") size");

            return readIDX(imageInput, labelInput, rows * columns, imagesCount);
        }
    }
}
