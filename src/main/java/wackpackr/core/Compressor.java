package wackpackr.core;

import java.io.IOException;

public interface Compressor
{
    String getName();

    byte[] compress(byte[] bytes) throws IOException;

    byte[] decompress(byte[] bytes) throws IOException;
}
