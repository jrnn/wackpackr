package wackpackr.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import wackpackr.io.BinaryIO;

/**
 * Horrible quick-and-dirty LZ77 compressor, at this point only for personal learning purposes ...
 *
 * @author Juho Juurinen
 */
public class LZSS
{
    // sliding window has 4096 bytes at once; i.e. offset can be max 4096
    private static final int PREFIX_SIZE = 4096;

    // because pointer takes 2 bytes, minimum length to encode is 3 bytes ("break-even point")
    private static final int THRESHOLD_LENGTH = 2;

    // because only length 3 and above is encoded, lookahead buffer has 16 + 2 bytes at once; i.e.
    // length can be [3, 18]. this needs to be taken into account when writing/reading pointers
    private static final int BUFFER_SIZE = 16 + THRESHOLD_LENGTH;

    public static byte[] compress(byte[] bytes) throws IOException
    {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            try (BinaryIO io = new BinaryIO(out))
            {
                process(bytes, io);
                return out.toByteArray();
            }
        }
    }

    private static void process(byte[] bytes, BinaryIO io) throws IOException
    {
        int pos = 0;
        while (pos < bytes.length)
        {
            int bestLength = 0;
            int bestOffset = 0;
            int maxOffset = Math.min(PREFIX_SIZE, pos);
            for (int offset = 1; offset <= maxOffset; offset++)
            {
                int length = 0;
                int bufferPos = pos;
                int prefixPos = pos - offset;
                int maxBufferPos = Math.min(pos + BUFFER_SIZE, bytes.length);
                while (bufferPos < maxBufferPos)
                {
                    if (bytes[bufferPos] != bytes[prefixPos])
                        break;
                    length++;
                    bufferPos++;
                    prefixPos++;
                }
                if (bestLength < length)
                {
                    bestLength = length;
                    bestOffset = offset;
                }
            }
            if (bestLength > THRESHOLD_LENGTH)
            {
                System.out.print("(" + bestOffset + "," + bestLength + ")");
                writePointer(bestOffset, bestLength - THRESHOLD_LENGTH, io);
                pos += bestLength;
            }
            else
            {
                System.out.print((char) bytes[pos]);
                io
                        .writeBit(false)
                        .writeByte(bytes[pos]);
                pos++;
            }
        }
    }

    private static void writePointer(int offset, int length, BinaryIO io) throws IOException
    {
        io
                .writeBit(true)
                .writeByte((byte) (offset >> 4))
                .writeByte((byte) (offset << 4 | length));
    }

    private static byte[] pointerToBytes(int offset, int length)
    {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (offset >> 4);
        bytes[1] = (byte) (offset << 4 | length);
        return bytes;
    }

    private static int readOffsetFromPointer(byte[] bytes)
    {
        return (bytes[0] << 4 | bytes[1] >> 4 & 0xF) & 0xFFF;
    }

    private static int readLengthFromPointer(byte[] bytes)
    {
        return bytes[1] & 0xF;
    }
}
