package wackpackr.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * Combined wrapper for I/O streams, that allows reading and writing an arbitrary number of bits at
 * once, irrespective of the byte boundaries of the underlying streams.
 *
 * <p>Possible to use with or without an input stream.</p>
 *
 * @author Juho Juurinen
 */
public class BinaryIO implements AutoCloseable
{
    private ByteArrayInputStream in = null;
    private int bufferIn = -1, offsetIn = 0;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private int bufferOut = 0, offsetOut = 0;

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
     * Reads and returns the requested number of bits next in the input stream, cast as an integer.
     *
     * @param bits number of bits to read
     * @return requested bits as an integer
     * @throws NullPointerException if no input stream has been set
     * @throws IOException if there's an error reading the input stream
     * @throws EOFException if the input stream is read through to the end during operation
     */
    public int readBits(int bits) throws IOException
    {
        int i = 0;

        for (; bits % 8 != 0; bits--)
            i = (i << 1) | (readBit() ? 1 : 0);

        for (; bits > 0; bits -= 8)
            i = (i << 8) | (readByte() & 0xFF);

        return i;
    }

    /**
     * Reads and returns the requested number of bytes next in the input stream.
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
            l = (l << 8) | (readByte() & 0xFF);

        return l;
    }

    /**
     * Writes one bit to the end of the output stream.
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
            bufferOut = offsetOut = 0;
        }

        return this;
    }

    /**
     * Writes one byte to the end of the output stream, irrespective of where the write pointer is
     * within the current byte buffer.
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
        {
            out.write(
                    (bufferOut << (8 - offsetOut)) |
                    ((b >> offsetOut) & (0xFF >> offsetOut))
            );
            bufferOut = b & ((1 << offsetOut) - 1);
        }

        return this;
    }

    /**
     * Writes the given value to the end of the output stream using the specified number of bits.
     * Note that this method does not check whether the value fits into the given bit size — trying
     * to write values with less bits than possible in practice corrupts the output stream.
     *
     * @param i value to write
     * @param bits number of bits to allocate for the value
     * @return a reference to this object
     * @throws IOException if there's an error writing to the output stream
     */
    public BinaryIO writeBits(int i, int bits) throws IOException
    {
        while (bits % 8 != 0)
        {
            bits--;
            writeBit(1 == ((i >> bits) & 1));
        }
        while (bits > 0)
        {
            bits -= 8;
            writeByte((byte) ((i >> bits) & 0xFF));
        }

        return this;
    }

    /**
     * Writes an arbitrary number of bytes to the end of the output stream.
     *
     * @param bs bytes to write
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
     * Writes a 32-bit chunk to the end of the output stream.
     *
     * @param l 32 bits of data to write as long
     * @return a reference to this object
     * @throws IOException if there's an error writing to the output stream
     */
    public BinaryIO write32Bits(long l) throws IOException
    {
        for (int k = 3; k >= 0; k--)
            writeByte((byte) (l >> (k * 8)));

        return this;
    }

    /**
     * Returns current contents of the underlying output stream.
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
