package wackpackr.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Combined wrapper for Input- and OutputStreams, that allows reading and writing one bit at a time,
 * while also offering the same methods for byte-size chunks irrespective of byte boundaries.
 *
 * TO-DO : More comprehensive comments ...
 *
 * @author Juho Juurinen
 */
public class BinaryIO implements AutoCloseable
{
    private final InputStream in;
    private final OutputStream out;

    private int bitsIn = 0;
    private int bitsOut = 0;
    private int bufferIn = -1;
    private int bufferOut = 0;

    public BinaryIO(InputStream in)
    {
        this.in = in;
        this.out = null;
    }

    public BinaryIO(OutputStream out)
    {
        this.in = null;
        this.out = out;
    }

    public BinaryIO(InputStream in, OutputStream out)
    {
        this.in = in;
        this.out = out;
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

        return ((prev << (8 - bitsIn)) | (bufferIn >> bitsIn)) % 256;  //  STILL SOMETHING FISHY HERE
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

    public void writeBit(boolean b) throws Exception
    {
        bufferOut = (bufferOut << 1) | (b ? 1 : 0);
        bitsOut++;

        if (bitsOut == 8)
        {
            out.write(bufferOut);
            bitsOut = 0;
            bufferOut = 0;
        }
    }

    public void writeByte(int b) throws Exception
    {
        assert 0 <= b && b < 256;

        if (bitsOut == 0)
            out.write(b);
        else
            for (int i = 7; i >= 0; i--)
                writeBit(((b >> i) & 1) == 1);
    }

    public void writeLong(long l) throws Exception
    {
        byte[] bs = new byte[4];

        for (int i = 3; i >= 0; i--)
        {
            bs[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        for (byte b : bs)
            writeByte(b);
    }

    @Override
    public void close() throws IOException
    {
        if (in != null)
            in.close();

        if (out != null)
            out.close();
    }
}
