package wackpackr.core;

import java.io.IOException;
import wackpackr.io.BinaryIO;
import wackpackr.util.HuffNode;

/**
 * Helper class that handles most of the steps in the Huffman algorithm: practically everything else
 * but trees, which are handled by {@link HuffTreeParser}.
 *
 * @author Juho Juurinen
 */
public class HuffEncoder
{
    /**
     * Table that pairs each encountered byte value (and the pseudo-EoF value) with its binary path
     * in the corresponding Huffman tree.
     */
    private static String[] CODES;

    /**
     * Index for the pseudo-EoF node's prefix code, outside the byte value range [ -128, 127 ].
     */
    private static final int EOF_INDEX = Byte.MAX_VALUE + 1;

    /**
     * Reads and decodes given input stream using the Huffman tree passed as parameter, at the same
     * time writing the decoded binary to the given output stream.
     *
     * Keeps on reading the input stream until expected pseudo-EoF marker is encountered.
     *
     * @param root pointer to root node of Huffman tree used in decoding
     * @param io I/O wrapper that holds both the input and output streams
     * @throws IOException
     */
    public static void decode(HuffNode root, BinaryIO io) throws IOException
    {
        while (true)
        {
            HuffNode node = root;
            while (!node.isLeaf())
                node = io.readBit()
                        ? node.getRight()
                        : node.getLeft();

            if (node.isEoF())
                break;

            io.writeByte(node.getValue());
        }
    }

    /**
     * Writes the given data in encoded -- and, hopefully, decompressed -- form into the given
     * output stream, using the given Huffman tree. Assumes that file identifier and the Huffman
     * tree have already been written into the output stream. Wraps the encoded data with the
     * pseudo-EoF node's prefix code on both sides, because this is the format expected when
     * decompressing. Finally, adds a few 0s for padding just to ensure that the last EoF sequence
     * is not partially cut out.
     *
     * @param bytes file to encode, as byte array
     * @param root pointer to root node of Huffman tree used in decoding
     * @param io I/O wrapper that holds both the input and output streams
     * @throws IOException
     */
    public static void encode(byte[] bytes, HuffNode root, BinaryIO io) throws IOException
    {
        CODES = new String[257];
        formCodeTable(root, "");
        write(EOF_INDEX, io);

        for (byte b : bytes)
            write(b, io);

        write(EOF_INDEX, io);
        io.writeByte((byte) 0);
    }


    /* --- Private helper methods below, no comments or description given. --- */


    private static void write(int value, BinaryIO io) throws IOException
    {
        for (char c : CODES[value + 128].toCharArray())
            if (c == '0' || c == '1')
                io.writeBit(c == '1');
            else
                throw new IOException();
    }

    private static void formCodeTable(HuffNode node, String code)
    {
        int i = (node.isEoF())
                ? EOF_INDEX
                : node.getValue();

        if (node.isLeaf())
            CODES[i + 128] = code;
        else
        {
            formCodeTable(node.getLeft(), code + "0");
            formCodeTable(node.getRight(), code + "1");
        }
    }
}
