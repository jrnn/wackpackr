package wackpackr.core;

import java.io.EOFException;
import java.io.IOException;
import wackpackr.config.Constants;
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
    private static boolean EOF_REACHED;

    /**
     * Reads and decodes given input stream, at the same time writing the decoded binary to the
     * given output stream.
     *
     * <p>Requires an {@link LZSSWindowOperator} instance to maintain a sliding window with random
     * access, in order to decode back references.</p>
     *
     * <p>Keeps on reading the input stream until expected pseudo-EoF marker is encountered.</p>
     *
     * @param io I/O wrapper that holds both the input and output streams
     * @param window sliding window operator configured for decoding purposes
     * @throws IOException if there's an error writing to or reading from the I/O streams
     */
    public static void decode(BinaryIO io, LZSSWindowOperator window) throws IOException
    {
        EOF_REACHED = false;

        while (!EOF_REACHED)
        {
            if (io.readBit())
                decodePointer(io, window);
            else
                decodeLiteral(io, window);
        }
    }

    /**
     * Writes the given data in encoded — and, hopefully, compressed — form into the given output
     * stream.
     *
     * <p>Requires an {@link LZSSWindowOperator} instance to control the "sliding window" dictionary
     * needed in encoding.</p>
     *
     * <p>Assumes that file identifier has already been written into the output stream.</p>
     *
     * <p>Closes the stream with a nonsensical "zero-offset" pointer as a pseudo-EoF indicator, plus
     * some 0s for padding to ensure the EoF sequence is not partially cut out.</p>
     *
     * @param io I/O wrapper that holds the output stream
     * @param window sliding window operator configured for encoding purposes
     * @throws IOException if there's an error writing to the output stream
     */
    public static void encode(BinaryIO io, LZSSWindowOperator window) throws IOException
    {
        while (window.next() != null)
        {
            int[] longestMatch = window.findLongestMatch();
            int offset = longestMatch[1];
            int length = (longestMatch[0] < Constants.LZSS_THRESHOLD_LENGTH)
                    ? 1
                    : longestMatch[0];

            if (length < Constants.LZSS_THRESHOLD_LENGTH)
                encodeLiteral(io, window.next());
            else
                encodePointer(io, offset, length - Constants.LZSS_THRESHOLD_LENGTH);

            for (int i = 0; i < length; i++)
                window.slideForward(nextByteOrNull(io));
        }

        encodeEoF(io);
    }


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


    private static void decodeLiteral(BinaryIO io, LZSSWindowOperator window) throws IOException
    {
        byte b = io.readByte();
        window.insertAndMove(b);
        io.writeByte(b);
    }

    private static void decodePointer(BinaryIO io, LZSSWindowOperator window) throws IOException
    {
        byte[] pointer = io.readBytes(2);

        int offset = (pointer[0] << 4 | pointer[1] >> 4 & 0xF) & 0xFFF;
        int length = (pointer[1] & 0xF) + Constants.LZSS_THRESHOLD_LENGTH;

        if (offset == 0)
            EOF_REACHED = true;
        else
            for (int i = 0; i < length; i++)
                io.writeByte(window.copyBackReference(offset - 1));
    }

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

    private static void encodeEoF(BinaryIO io) throws IOException
    {
        encodePointer(io, 0, 0);
        io.writeByte((byte) 0);
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
