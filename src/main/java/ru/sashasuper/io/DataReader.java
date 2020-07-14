package ru.sashasuper.io;

import java.io.IOException;

public abstract class DataReader {
    public abstract Dataset read() throws IOException;
}
