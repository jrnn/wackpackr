package wackpackr.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * Combined wrapper for I/O streams, that allows reading and writing one bit at a time, while also
 * offering the same methods for byte-size chunks, irrespective of the byte boundaries of the
 * underlying streams.
 *
 * <p>Possible to use with or without an input stream.</p>
 *
 * @author Juho Juurinen
 */
public class BinaryIO implements AutoCloseable
{
    private ByteArrayInputStream in = null;
    private int bufferIn = -1;
    private int offsetIn = 0;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private int bufferOut = 0;
    private int offsetOut = 0;

    /**
     * Constructs a new BinaryIO instance for writing purposes only, without an input stream.
     */
    public BinaryIO() {}

    /**
     * Constructs a new BinaryIO instance for both reading and writing purposes, with the given byte
     * array as the input stream.
     *
     * @param bytes input buffer
     */
    public BinaryIO(byte[] bytes)
    {
        this.in = new ByteArrayInputStream(bytes);
    }

    /**
     * Reads and returns the next bit in the input stream.
     *
     * @return the next bit in the input stream as boolean
     * @throws NullPointerException if no input stream has been set
     * @throws IOException if there's an error reading the input stream
     * @throws EOFException if the input stream has been read through to the end
     */
    public boolean readBit() throws IOException
    {
        if (offsetIn == 0)
        {
            bufferIn = in.read();
            offsetIn = 8;
        }
        if (bufferIn == -1)
            throw new EOFException();

        offsetIn--;
        return (((bufferIn >> offsetIn) & 1) == 1);
    }

    /**
     * Reads and returns the next byte in the input stream, irrespective of where the read pointer
     * is within current byte buffer.
     *
     * @return the next byte in the input stream
     * @throws NullPointerException if no input stream has been set
     * @throws IOException if there's an error reading the input stream
     * @throws EOFException if the input stream has been read through to the end
     */
    public byte readByte() throws IOException
    {
        int prev = bufferIn;
        bufferIn = in.read();

        if (bufferIn == -1)
            throw new EOFException();

        int b = (offsetIn == 0)
                ? bufferIn
                : (prev << (8 - offsetIn)) | (bufferIn >> offsetIn);

        return (byte) b;
    }

    /**
     * Reads and returns the next byte in the input stream, or {@code null} if the input stream has
     * already been read through to the end.
     *
     * @return the next byte in input stream, or null if the input stream has reached the end
     * @throws NullPointerException if no input stream has been set
     * @throws IOException if there's an error reading the input stream
     */
    public Byte readByteOrNull() throws IOException
    {
        try
        {
            return readByte();
        }
        catch (EOFException e) {}

        return null;
    }

    /**
     * Reads and returns requested number of bytes coming up next in the input stream.
     *
     * @param count number of bytes to read
     * @return requested number of bytes as array
     * @throws NullPointerException if no input stream has been set
     * @throws IOException if there's an error reading the input stream
     * @throws EOFException if the input stream is read through to the end during operation
     */
    public byte[] readBytes(int count) throws IOException
    {
        byte[] bs = new byte[count];

        for (int i = 0; i < count; i++)
            bs[i] = readByte();

        return bs;
    }

    /**
     * Reads and returns the next 16-bit chunk in the input stream, cast as an integer.
     *
     * @return the next 16 bits in the input stream as integer
     * @throws NullPointerException if no input stream has been set
     * @throws IOException if there's an error reading the input stream
     * @throws EOFException if there is less than 16 bits left in the input stream
     */
    public int read16Bits() throws IOException
    {
        int i = (readByte() & 0xFF);

        i <<= 8;
        i |= (readByte() & 0xFF);

        return i;
    }

    /**
     * Reads and returns the next 32-bit chunk in the input stream, cast as a long value.
     *
     * @return the next 32 bits in the input stream as long
     * @throws NullPointerException if no input stream has been set
     * @throws IOException if there's an error reading the input stream
     * @throws EOFException if there is less than 32 bits left in the input stream
     */
    public long read32Bits() throws IOException
    {
        long l = 0L;

        for (int k = 0; k < 4; k++)
        {
            l <<= 8;
            l |= (readByte() & 0xFF);
        }

        return l;
    }

    /**
     * Writes one bit at the end of the output stream.
     *
     * @param b bit to write as boolean
     * @return a reference to this object
     * @throws IOException if there's an error writing to the output stream
     */
    public BinaryIO writeBit(boolean b) throws IOException
    {
        bufferOut = (bufferOut << 1) | (b ? 1 : 0);
        offsetOut++;

        if (offsetOut == 8)
        {
            out.write(bufferOut);
            offsetOut = 0;
            bufferOut = 0;
        }

        return this;
    }

    /**
     * Writes one byte to the end of the output stream, irrespective of where the write pointer is
     * within current byte buffer.
     *
     * @param b byte to write
     * @return a reference to this object
     * @throws IOException if there's an error writing to the output stream
     */
    public BinaryIO writeByte(byte b) throws IOException
    {
        if (offsetOut == 0)
            out.write(b);
        else
            for (int i = 7; i >= 0; i--)
                writeBit(((b >> i) & 1) == 1);

        return this;
    }

    /**
     * Writes an arbitrary number of bytes to the end of the output stream.
     *
     * @param bs bytes to write, as array
     * @return a reference to this object
     * @throws IOException if there's an error writing to the output stream
     */
    public BinaryIO writeBytes(byte[] bs) throws IOException
    {
        for (byte b : bs)
            writeByte(b);

        return this;
    }

    /**
     * Writes a 16-bit chunk to the end of the output stream.
     *
     * @param i 16 bits of data to write, as integer
     * @return a reference to this object
     * @throws IOException if there's an error writing to the output stream
     */
    public BinaryIO write16Bits(int i) throws IOException
    {
        writeByte((byte) (i >> 8));
        writeByte((byte) (i & 0xFF));

        return this;
    }

    /**
     * Writes a 32-bit chunk to the end of the output stream.
     *
     * @param l 32 bits of data to write, as long
     * @return a reference to this object
     * @throws IOException if there's an error writing to the output stream
     */
    public BinaryIO write32Bits(long l) throws IOException
    {
        byte[] bs = new byte[4];

        for (int k = 3; k >= 0; k--)
        {
            bs[k] = (byte) (l & 0xFF);
            l >>= 8;
        }

        writeBytes(bs);
        return this;
    }

    /**
     * Reads and returns current contents of the underlying output stream.
     *
     * @return contents of the output stream, as byte array
     */
    public byte[] getBytesOut()
    {
        return out.toByteArray();
    }

    @Override
    public void close() throws IOException
    {
        if (in != null)
            in.close();

        out.close();
    }
}
