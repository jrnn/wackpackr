package wackpackr.core;

import java.io.IOException;

/**
 * @author Juho Juurinen
 */
public interface Compressor
{
    byte[] compress(byte[] bytes) throws IOException;

    byte[] decompress(byte[] bytes) throws IOException;
}
