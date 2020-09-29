package ru.sashasuper.io;
import ru.sashasuper.logic.Network;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NetworkSerializer {

    public static void serialization(String fileName, Network net) throws IOException {
        OutputStream outputStream = new GZIPOutputStream(new FileOutputStream(fileName));
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        objectOutputStream.writeObject(net);
        objectOutputStream.close();
    }

    public static Network deserialization(String fileName) throws IOException, ClassNotFoundException {
        InputStream fileInputStream = new GZIPInputStream(new FileInputStream(fileName));
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        Network network = (Network) objectInputStream.readObject();
        objectInputStream.close();
        return network;
    }
}



