package wackpackr.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import wackpackr.io.BinaryIO;

/**
 * Compression and decompression with a simplistic implementation of the LZSS algorithm. This class
 * basically just handles initialising the input and output streams used for I/O operations, while
 * most of the actual work is delegated to helper classes.
 *
 * @author Juho Juurinen
 */
public class LZSS
{
    /**
     * 32-bit identifier placed at the head of compressed files.
     */
    private static final long LZSS_TAG = 0x07072017;

    // Pointer reserves 12 bits for offset, i.e. range [0, 4095]. "Zero-offset" is used as EoF
    // marker. So, dictionary can have max 4095 bytes at once.
    private static final int PREFIX_SIZE = 4095;

    // because pointer takes 2 bytes, minimum length to encode is 3 bytes ("break-even point")
    private static final int THRESHOLD_LENGTH = 3;

    // because only length 3 and above is encoded, lookahead buffer has 16 + 2 bytes at once; i.e.
    // length can be [3, 18]. this needs to be taken into account when writing/reading pointers
    private static final int BUFFER_SIZE = 15 + THRESHOLD_LENGTH;

    public static byte[] compress(byte[] bytes) throws IOException
    {
        try (
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            try (BinaryIO io = new BinaryIO(in, out))
            {
                io.write32Bits(LZSS_TAG);

                LZSSEncoder.encode(
                        io,
                        new LZSSWindowOperator()
                );
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

                LZSSEncoder.decode(
                        io,
                        new LZSSWindowOperator()
                );
                return out.toByteArray();
            }
        }
    }
}
