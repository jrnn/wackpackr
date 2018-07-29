package wackpackr.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for InputStream that allows reading one bit at a time, while also offering a method for
 * reading byte-size chunks irrespective of byte boundaries.
 *
 * TO-DO : Bake in a similar wrapper for OutputStream into the same class(?)
 *
 * @author Juho Juurinen
 */
public class BinaryIO implements AutoCloseable
{
    private InputStream in;
    private int bitsIn = 0;
    private int bufferIn = -1;

    //private OutputStream out;
    //private int bitsOut = 0;
    //private int bufferOut = 0;

    public void setInputStream(InputStream is) throws IOException
    {
        if (in == null)
            in = is;
    }

    /**
     * Reads and returns the next bit in input stream.
     *
     * @return the next bit in input stream as boolean
     * @throws NullPointerException if no input stream has been set
     * @throws IOException if there's an error reading the input stream
     * @throws EOFException if input stream has been read through to the end
     */
    public boolean readBit() throws Exception
    {
        if (bitsIn == 0)
        {
            bufferIn = in.read();
            bitsIn = 8;
        }
        if (bufferIn == -1)
            throw new EOFException();

        bitsIn--;
        return ((bufferIn >> bitsIn) & 1) == 1;
    }

    /**
     * Reads and returns the next byte in input stream, irrespective of where the read pointer is
     * within current byte buffer (bitwise trickery).
     *
     * @return the next byte in input stream as integer value
     * @throws NullPointerException if no input stream has been set
     * @throws IOException if there's an error reading the input stream
     * @throws EOFException if input stream has been read through to the end
     */
    public int readByte() throws Exception
    {
        int prev = bufferIn;
        bufferIn = in.read();

        if (bufferIn == -1)
            throw new EOFException();

        return (prev << (8 - bitsIn)) | (bufferIn >> bitsIn);
    }

    /**
     * Reads and returns the next 32-bit chunk in input stream cast as a long value.
     *
     * @return the next 32 bits in input stream as long value
     * @throws NullPointerException if no input stream has been set
     * @throws IOException if there's an error reading the input stream
     * @throws EOFException if there is less than 32 bits left in input stream
     */
    public long readLong() throws Exception
    {
        return (readByte() << 24 |
                readByte() << 16 |
                readByte() <<  8 |
                readByte());
    }

    @Override
    public void close() throws IOException
    {
        if (in != null)
            in.close();

        bitsIn = 0;
        bufferIn = -1;
    }
}
