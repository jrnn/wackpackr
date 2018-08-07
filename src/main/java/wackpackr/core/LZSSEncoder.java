package wackpackr.core;

import java.io.EOFException;
import java.io.IOException;
import wackpackr.io.BinaryIO;

/**
 * Helper class that controls the I/O part of the LZSS algorithm: namely, encoding raw data into a
 * stream of literal and pointer blocks, and translating decoded data back to its original form.
 * This class depends on {@link LZSSWindowOperator} to handle the sliding window.
 *
 * @author Juho Juurinen
 */
public class LZSSEncoder
{
    // CONSTANTS SHOULD GO INTO ONE PLACE
    private static final int THRESHOLD_LENGTH = 3;

    /**
     * Writes the given data in encoded -- and, hopefully, compressed -- form into the given output
     * stream. Requires an {@link LZSSWindowOperator} instance to control the "sliding window"
     * dictionary needed in encoding. Assumes that file identifier has already been written into the
     * output stream. Closes the stream with a nonsensical "zero-offset" pointer as a pseudo-EoF
     * indicator, plus some 0s for padding to ensure the EoF sequence is not partially cut out.
     *
     * @param io I/O wrapper that holds both the input and output streams
     * @param window
     * @throws IOException
     */
    public static void encode(BinaryIO io, LZSSWindowOperator window) throws IOException
    {
        while (!window.isBufferFull())
            window.insert(nextByteOrNull(io));

        while (window.next() != null)
        {
            int[] longestMatch = window.findLongestMatch();
            int offset = longestMatch[1];
            int length = (longestMatch[0] < THRESHOLD_LENGTH)
                    ? 1
                    : longestMatch[0];

            if (length < THRESHOLD_LENGTH)
                encodeLiteral(io, window.next());
            else
                encodePointer(io, offset, length - THRESHOLD_LENGTH);

            for (int i = 0; i < length; i++)
                window.slideForward(nextByteOrNull(io));
        }

        encodePointer(io, 0, 0);
        io.writeByte((byte) 0);
    }


    /* --- Private helper methods below, no comments or description given. --- */


    private static void encodeLiteral(BinaryIO io, byte b) throws IOException
    {
        io
                .writeBit(false)
                .writeByte(b);
    }

    private static void encodePointer(BinaryIO io, int offset, int length) throws IOException
    {
        io
                .writeBit(true)
                .writeByte((byte) (offset >> 4))
                .writeByte((byte) (offset << 4 | length));
    }

    private static Byte nextByteOrNull(BinaryIO io) throws IOException
    {
        try
        {
            return io.readByte();
        }
        catch (EOFException e) {}

        return null;
    }
}
