package wackpackr.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayDeque;
import wackpackr.io.BinaryIO;
import wackpackr.util.SlidingWindow;

/**
 * Horrible quick-and-dirty LZ77 compressor, at this point only for personal learning purposes ...
 * NOTE THAT THIS IS ALL HEAVILY, HEAVILY WORK-IN-PROGRESS, SIGNIFICANT REFACTORING AHEAD
 *
 * @author Juho Juurinen
 */
public class LZSS
{
    /**
     * 32-bit identifier placed at the head of compressed files.
     */
    private static final long LZSS_TAG = 0x07072017;

    // sliding window has 4096 bytes at once; i.e. offset can be max 4096
    private static final int PREFIX_SIZE = 4096;

    // because pointer takes 2 bytes, minimum length to encode is 3 bytes ("break-even point")
    private static final int THRESHOLD_LENGTH = 2;

    // because only length 3 and above is encoded, lookahead buffer has 16 + 2 bytes at once; i.e.
    // length can be [3, 18]. this needs to be taken into account when writing/reading pointers
    private static final int BUFFER_SIZE = 16 + THRESHOLD_LENGTH;

    public static byte[] compress(byte[] bytes) throws IOException
    {
        try (
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            try (BinaryIO io = new BinaryIO(in, out))
            {
                io.write32Bits(LZSS_TAG);

                compress(io);
                return out.toByteArray();
            }
        }
    }

    public static byte[] decompress(byte[] bytes) throws IOException
    {
        try (
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            try (BinaryIO io = new BinaryIO(in, out))
            {
                if (io.read32Bits() != LZSS_TAG)
                    throw new IllegalArgumentException("Not a LZSS compressed file");

                decompress(io);
                return out.toByteArray();
            }
        }
    }

    private static void compress(BinaryIO io) throws IOException
    {
        SlidingWindow<Byte> sw = new SlidingWindow(PREFIX_SIZE + BUFFER_SIZE);
        ArrayDeque<Integer>[] Q = new ArrayDeque[256];

        for (int i = 0; i < 256; i++)
            Q[i] = new ArrayDeque<>();

        for (int i = 0; i < BUFFER_SIZE; i++)
            ioToSw(sw, io);

        while (sw.available() > -1)
        {
            int bestLength = 0;
            int bestOffset = 0;

            //System.out.println("head of buffer = [" + ((char) (int) sw.read()) + "] start positions in prefix = " + Q[sw.read() + 128]);

            for (Integer pos : Q[sw.read() + 128])
            {
                int length = 0;
                int offset = sw.cursor() - pos;
                int maxLength = Math.min(sw.available(), BUFFER_SIZE);

                while (length <= maxLength)
                {
                    if (!sw.read(length - offset).equals(sw.read(length)))
                        break;
                    length++;
                }
                if (bestLength < length)
                {
                    bestLength = length;
                    bestOffset = offset;
                }
            }
            if (bestLength > THRESHOLD_LENGTH)
            {
                //System.out.print("(" + bestOffset + "," + bestLength + ")");
                writePointer(bestOffset, bestLength - THRESHOLD_LENGTH - 1, io);
            }
            else
            {
                //System.out.print((char) (byte) sw.read());
                io
                        .writeBit(false)
                        .writeByte(sw.read());
                bestLength = 1;
            }
            for (int q = 0; q < bestLength; q++)  // "slide forward" one step
            {
                if (sw.last() != null)
                    Q[sw.last() + 128].poll();
                Q[sw.read() + 128].offer(sw.cursor());

                try
                {
                    sw.insert((byte) io.readByte());
                } catch (EOFException e) {}

                sw.move();
            }
        }
        // use a "pointless pointer" (0-0) as EoF marker + pad with a few zeroes
        writePointer(0, 0, io);
        io.writeByte((byte) 0);
        //System.out.println("(0,0)");
    }

    private static void ioToSw(SlidingWindow sw, BinaryIO io) throws IOException
    {
        try
        {
            sw.insert((byte) io.readByte());
        }
        catch (EOFException e) {}
    }

    private static void decompress(BinaryIO io) throws IOException
    {
        SlidingWindow<Byte> sw = new SlidingWindow(PREFIX_SIZE + BUFFER_SIZE);
        int length, offset;
        byte b;
        byte[] pointer = new byte[2];
        while (true)
        {
            if (io.readBit())
            {
                pointer[0] = io.readByte();
                pointer[1] = io.readByte();
                offset = readOffsetFromPointer(pointer);
                length = readLengthFromPointer(pointer) + THRESHOLD_LENGTH + 1;
                if (offset == 0)    // eof
                    break;
                while (length > 0)
                {
                    b = sw.read(-offset);
                    sw.insert(b);
                    sw.move();
                    io.writeByte(b);
                    length--;
                    //System.out.print((char) b);
                }
            }
            else
            {
                b = io.readByte();
                sw.insert(b);
                sw.move();
                io.writeByte(b);
                //System.out.print((char) b);
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
