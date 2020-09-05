package ru.sashasuper.io;

import ru.sashasuper.logic.Vector;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static java.util.AbstractMap.SimpleEntry;
import static ru.sashasuper.utils.Assertions.thr;

public class IDXReader extends DataReader {
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

    protected int readUnsignedInt(InputStream inImage) throws IOException {
        long temp = (inImage.read() << 24) | (inImage.read() << 16) | (inImage.read() << 8) | (inImage.read());
        thr(temp < 0, "Wrong value for unsigned integer: " + temp);
        return (int) temp;
    }

    private byte[] buf = null;
    // Сразу переводит в негатив, где 255 - белый
    protected Vector readImageInVector(InputStream inImage, int length) throws IOException {
        if(buf == null || buf.length == length) {
            buf = new byte[length];
            int read = inImage.readNBytes(buf, 0, length);
            if(read != length)
                throw new IOException("File is corrupted");
        }
        float[] array = new float[length];

        for (int pixel = 0; pixel < length; pixel++)
             array[pixel] = Byte.toUnsignedInt(buf[pixel]) / 255f * 0.99f;

        return new Vector(array);
    }

    protected List<SimpleEntry<Vector, Integer>> readIDX
            (InputStream imageInput, InputStream labelInput, int pixels, int images) throws IOException {

        List<SimpleEntry<Vector, Integer>> result = new ArrayList<>(images);
        for(int i = 0; i < images; i++) {
            Vector vector = readImageInVector(imageInput, pixels);

            int label = labelInput.read();
            thr(label < 0, "Unexpected end of file");

            result.add(new SimpleEntry<>(vector, label));
        }
        return result;
    }

    public Dataset read() throws IOException {
        FileInputStream fisImages = new FileInputStream(imagesFilePath);
        FileInputStream fisLabels = new FileInputStream(labelsFilePath);

        try(InputStream imageInput = gzippedImages
                    ? new GZIPInputStream(new BufferedInputStream(fisImages))
                    : new BufferedInputStream(fisImages);

            InputStream labelInput = gzippedLabels
                    ? new GZIPInputStream(new BufferedInputStream(fisLabels))
                    : new BufferedInputStream(fisLabels)) {

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

            List<SimpleEntry<Vector, Integer>> list = readIDX(imageInput, labelInput, rows * columns, imagesCount);
            return new IDXDataset(list);
        }
    }
}
