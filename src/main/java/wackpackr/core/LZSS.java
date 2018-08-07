package wackpackr.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import wackpackr.config.Constants;
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
     * Compresses given file. Writes a 32-bit identifier at the head of the compressed binary, and
     * a pseudo-EoF indicator at the end, padded with a few 0s (just to be safe).
     *
     * @param bytes file to compress, as byte array
     * @return compressed file, as byte array
     * @throws IOException
     */
    public static byte[] compress(byte[] bytes) throws IOException
    {
        try (
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            try (BinaryIO io = new BinaryIO(in, out))
            {
                io.write32Bits(Constants.LZSS_TAG);

                LZSSEncoder.encode(
                        io,
                        new LZSSWindowOperator()
                );
                return out.toByteArray();
            }
        }
    }

    /**
     * Decompresses given file. Apart from checking the 32-bit tag in the header, there are
     * practically no other measures to verify the file. Passing in a valid file is method caller's
     * responsibility.
     *
     * @param bytes file to decompress, as byte array
     * @return decompressed file, as byte array
     * @throws IllegalArgumentException if file does not have the correct identifier in its header
     * @throws IOException
     */
    public static byte[] decompress(byte[] bytes) throws IOException
    {
        try (
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            try (BinaryIO io = new BinaryIO(in, out))
            {
                if (io.read32Bits() != Constants.LZSS_TAG)
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
