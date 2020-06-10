package ru.sashasuper.logic;

import java.io.IOException;

public abstract class DataReader {
    public abstract Dataset read() throws IOException;
}
