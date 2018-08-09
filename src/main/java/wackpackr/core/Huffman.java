package wackpackr.core;

import java.io.IOException;
import wackpackr.config.Constants;
import wackpackr.io.BinaryIO;
import wackpackr.util.HuffNode;

/**
 * Compression and decompression with a simplistic implementation of the Huffman algorithm. This
 * class basically just handles initialising the input- and output streams used for I/O operations,
 * while most of the actual work is delegated to helper classes.
 *
 * @author Juho Juurinen
 */
public class Huffman
{
    /**
     * Compresses given file. Information needed for decompression is stored to the output file as a
     * header. The header consists, in order, of (1) a 32-bit identifier; (2) Huffman tree that maps
     * prefix codes to byte values; and (3) prefix code associated with the pseudo-EoF marker.
     * Header is followed by the actual data in encoded form. File closes with the pseudo-EoF bit
     * sequence, and a few 0s for buffer (just to be safe).
     *
     * @param bytes file to compress, as byte array
     * @return compressed file, as byte array
     * @throws IOException
     */
    public static byte[] compress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO())
        {
            io.write32Bits(Constants.HUFFMAN_TAG);

            HuffNode root = HuffTreeParser.buildTree(bytes);
            HuffTreeParser.encodeTree(root, io);

            HuffEncoder.encode(bytes, root, io);
            return io.getBytesOut();
        }
    }

    /**
     * Decompresses given file. Tries first to read file header, which contains all information
     * needed for decompression; then decodes the compressed data with the Huffman tree extracted
     * from the header.
     *
     * Apart from checking the 32-bit tag in the header, there are practically no other measures to
     * verify the file. Passing in a valid file is method caller's responsibility.
     *
     * @param bytes file to decompress, as byte array
     * @return decompressed file, as byte array
     * @throws IllegalArgumentException if file does not have the correct identifier in its header
     * @throws IOException
     */
    public static byte[] decompress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO(bytes))
        {
            if (io.read32Bits() != Constants.HUFFMAN_TAG)
                throw new IllegalArgumentException("Not a Huffman compressed file");

            HuffNode root = HuffTreeParser.decodeTree(io);
            HuffEncoder.decode(root, io);

            return io.getBytesOut();
        }
    }
}
