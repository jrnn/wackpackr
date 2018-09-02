package wackpackr.core;

import java.io.EOFException;
import java.io.IOException;
import wackpackr.io.BinaryIO;
import wackpackr.util.ByteString;

/**
 * Compression and decompression with a simplistic implementation of the LZW algorithm.
 *
 * <p>Unlike in the other compressor classes, here it made no sense to decouple the encoding and
 * decoding operations to helper methods. Hence the fairly bloated main methods.</p>
 *
 * @author Juho Juurinen
 */
public class LZWCompressor implements Compressor
{
    private static final long LZW_TAG = 0x04092009;

    /**
     * Compresses given file using dynamic (variable bit size) LZW encoding.
     *
     * <p>Writes a 32-bit identifier, indicating the used compression technique, to the beginning
     * of the compressed binary, followed by the actual data in encoded form. Ends with a pseudo-
     * EoF marker (zero index), and a few 0s to ensure that the EoF bit sequence is not partially
     * cut off.</p>
     *
     * @param bytes file to compress as byte array
     * @return compressed file as byte array
     * @throws IOException if there's an error writing to the output stream
     */
    @Override
    public byte[] compress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO())
        {
            io.write32Bits(LZW_TAG);

            LZWDictionary dict = new LZWDictionary();
            int bitsize = 9, index = -1, newIndex;

            for (byte b : bytes)
            {
                newIndex = dict.get(index, b);

                if (newIndex > 0)
                    index = newIndex;
                else
                {
                    io.writeBits(index, bitsize);
                    bitsize = dict.put(index, b);
                    index = dict.get(-1, b);
                }
                if (dict.isFull())
                    dict.reset();
            }
            io
                    .writeBits(index, bitsize)
                    .write32Bits(0);  // EoF marker

            return io.getBytesOut();
        }
    }

    /**
     * Decompresses given file using dynamic (variable bit size) LZW decoding.
     *
     * <p>Apart from checking the 32-bit tag in the header, there are practically no other measures
     * to verify the file. Passing in a valid file is method caller's responsibility.</p>
     *
     * <p>Keeps on reading the input stream until a pseudo-EoF marker (zero dictionary index) is
     * encountered. Throws {@code EOFException} if no such marker is seen before reaching the end of
     * the input stream.</p>
     *
     * @param bytes file to decompress as byte array
     * @return decompressed file as byte array
     * @throws IllegalArgumentException if file does not have the correct identifier in its header
     * @throws EOFException if no pseudo-EoF marker is present in the input stream
     * @throws IOException if there's an error writing to or reading from the I/O streams
     */
    @Override
    public byte[] decompress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO(bytes))
        {
            if (io.read32Bits() != LZW_TAG)
                throw new IllegalArgumentException("Not a LZW compressed file");

            LZWDictionary dict = new LZWDictionary();
            ByteString x, y;

            int bitsize = 9;
            int index = io.readBits(bitsize);
            int newIndex = io.readBits(bitsize);
            io.writeBytes(dict.get(index).getBytes());

            while (newIndex != 0)
            {
                x = dict.get(index).copy();
                y = dict.get(newIndex);

                if (y != null)
                {
                    bitsize = dict.put(x.append(y.byteAt(0)));
                    io.writeBytes(y.getBytes());
                }
                else
                {
                    bitsize = dict.put(x.append(x.byteAt(0)));
                    io.writeBytes(x.getBytes());
                }
                if (dict.isFull())
                {
                    dict.reset();
                    bitsize = 9;
                }

                index = newIndex;
                newIndex = io.readBits(bitsize);
            }

            return io.getBytesOut();
        }
    }

    @Override
    public String getName()
    {
        return "LZW";
    }
}
