package wackpackr.core;

import java.io.EOFException;
import java.io.IOException;
import wackpackr.config.Constants;
import wackpackr.io.BinaryIO;

/**
 * Compression and decompression with a simplistic implementation of the LZSS algorithm.
 *
 * @author Juho Juurinen
 */
public class LZSSCompressor implements Compressor
{
    private static boolean EOF_REACHED;
    private static LZSSWindowOperator WINDOW;

    /**
     * Compresses given file using LZSS encoding.
     *
     * <p>Writes a 32-bit identifier, indicating the used compression technique, to the beginning
     * of the compressed binary, followed by the actual data in encoded form. Closes with a
     * nonsensical "zero-offset" pointer as a pseudo-EoF marker, plus a few 0s to ensure that the
     * EoF bit sequence is not partially cut off.</p>
     *
     * @param bytes file to compress as byte array
     * @return compressed file as byte array
     * @throws IOException if there's an error writing to the output stream
     */
    @Override
    public byte[] compress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO(bytes))
        {
            io.write32Bits(Constants.LZSS_TAG);

            byte[] initialBuffer = io.readBytes(Constants.LZSS_BUFFER_SIZE);
            WINDOW = new LZSSWindowOperator(initialBuffer);

            while (WINDOW.peek() != null)
                encode(io);

            io      // EoF marker
                    .writeBit(true)
                    .writeBytes(new byte[]{ 0, 0, 0 });

            return io.getBytesOut();
        }
    }

    /**
     * Decompresses given file using LZSS decoding.
     *
     * <p>Apart from checking the 32-bit tag in the header, there are practically no other measures
     * to verify the file. Passing in a valid file is method caller's responsibility.</p>
     *
     * <p>Keeps on reading the input stream until a pseudo-EoF marker ("zero-offset" pointer) is
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
            if (io.read32Bits() != Constants.LZSS_TAG)
                throw new IllegalArgumentException("Not a LZSS compressed file");

            WINDOW = new LZSSWindowOperator();
            EOF_REACHED = false;

            while (!EOF_REACHED)
                decode(io);

            return io.getBytesOut();
        }
    }

    @Override
    public String getName()
    {
        return "LZSS";
    }


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


    private void decode(BinaryIO io) throws IOException
    {
        if (io.readBit())
        {   // pointer block
            byte[] pointer = io.readBytes(2);
            int offset = (pointer[0] << 4 | pointer[1] >> 4 & 0xF) & 0xFFF;
            int length = (pointer[1] & 0xF) + Constants.LZSS_THRESHOLD_LENGTH;

            if (offset == 0)
                EOF_REACHED = true;
            else
                for (int i = 0; i < length; i++)
                    io.writeByte(WINDOW.copyBackReference(offset - 1));
        }
        else
        {   // literal block
            byte b = io.readByte();
            WINDOW.insertAndMove(b);
            io.writeByte(b);
        }
    }

    private void encode(BinaryIO io) throws IOException
    {
        int[] longestMatch = WINDOW.findLongestMatch();
        int length = longestMatch[0], offset = longestMatch[1];

        if (length < Constants.LZSS_THRESHOLD_LENGTH)
        {
            io      // literal block
                    .writeBit(false)
                    .writeByte(WINDOW.peek());
            length = 1;
        }
        else
            io      // pointer block
                    .writeBit(true)
                    .writeByte((byte) (offset >> 4))
                    .writeByte((byte) (offset << 4 | (length - Constants.LZSS_THRESHOLD_LENGTH)));

        for (int i = 0; i < length; i++)
            WINDOW.slideForward(io.readByteOrNull());
    }
}
