package wackpackr.core;

import java.io.EOFException;
import java.io.IOException;
import wackpackr.config.Constants;
import wackpackr.io.BinaryIO;
import wackpackr.util.HuffNode;

/**
 * Compression and decompression with a simplistic implementation of the Huffman algorithm.
 *
 * @author Juho Juurinen
 */
public class HuffCompressor implements Compressor
{
    private static final int EOF_INDEX = 256;
    private static boolean EOF_REACHED;
    private static final String[] CODES = new String[EOF_INDEX + 1];

    /**
     * Compresses the given file using vanilla Huffman encoding.
     *
     * <p>Information needed for decompression is encoded to the beginning of the compressed binary.
     * This header consists, in order, of:</p><ol><li>a 32-bit identifier indicating the used
     * compression technique</li><li>Huffman tree that maps prefix codes to byte values</li><li>
     * prefix code associated with the pseudo-EoF marker</li></ol>
     *
     * <p>The header is followed by the actual data in encoded form. The compressed binary closes
     * with the pseudo-EoF marker and, finally, a few 0s for padding — just to ensure that the EoF
     * bit sequence is not partially cut off.</p>
     *
     * @param bytes file to compress, as byte array
     * @return compressed file, as byte array
     * @throws IOException if there's an error writing to the output stream
     */
    @Override
    public byte[] compress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO())
        {
            io.write32Bits(Constants.HUFFMAN_TAG);

            HuffNode root = HuffTreeParser.buildTree(bytes);
            HuffTreeParser.encodeTree(root, io);

            formCodeTable(root, "");
            encode(CODES[EOF_INDEX], io);

            for (byte b : bytes)
                encode(CODES[b + 128], io);

            encode(CODES[EOF_INDEX], io);
            io.writeByte((byte) 0);

            return io.getBytesOut();
        }
    }

    /**
     * Decompresses the given file using vanilla Huffman decoding.
     *
     * <p>Tries first to read file header, which should contain all information needed for
     * decompression; then decodes the compressed data using the Huffman tree extracted from the
     * header.</p>
     *
     * <p>Apart from checking the 32-bit tag in the header, there are practically no other measures
     * to verify the file. Passing in a valid file is method caller's responsibility.</p>
     *
     * <p>Keeps on reading the input stream until a pseudo-EoF marker (as instructed by the header)
     * is encountered. Throws {@code EOFException} if no such marker is seen before reaching the end
     * of the input stream.</p>
     *
     * @param bytes file to decompress, as byte array
     * @return decompressed file, as byte array
     * @throws IllegalArgumentException if file does not have the correct identifier in its header
     * @throws EOFException if expected pseudo-EoF marker is not present in the input stream
     * @throws IOException if there's an error writing to or reading from the I/O streams
     */
    @Override
    public byte[] decompress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO(bytes))
        {
            if (io.read32Bits() != Constants.HUFFMAN_TAG)
                throw new IllegalArgumentException("Not a Huffman compressed file");

            HuffNode root = HuffTreeParser.decodeTree(io);
            EOF_REACHED = false;

            while (!EOF_REACHED)
                decode(root, io);

            return io.getBytesOut();
        }
    }

    @Override
    public String getName() {
        return "Huffman";
    }


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


    private void decode(HuffNode node, BinaryIO io) throws IOException
    {
        while (!node.isLeaf())
            node = io.readBit()
                    ? node.getRight()
                    : node.getLeft();

        if (node.isEoF())
            EOF_REACHED = true;
        else
            io.writeByte(node.getValue());
    }

    private void encode(String code, BinaryIO io) throws IOException
    {
        for (char c : code.toCharArray())
            io.writeBit(c == '1');
    }

    private void formCodeTable(HuffNode node, String code)
    {
        if (node.isEoF())
            CODES[EOF_INDEX] = code;
        else if (node.isLeaf())
            CODES[node.getValue() + 128] = code;
        else
        {
            formCodeTable(node.getLeft(),  code + "0");
            formCodeTable(node.getRight(), code + "1");
        }
    }
}
