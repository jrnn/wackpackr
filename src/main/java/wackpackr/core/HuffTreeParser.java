package wackpackr.core;

import java.io.IOException;
import wackpackr.io.BinaryIO;
import wackpackr.util.HuffNode;
import wackpackr.util.MinHeap;

/**
 * Helper class that handles forming and parsing of frequency-sorted prefix trees needed in Huffman
 * compression and decompression.
 *
 * @author Juho Juurinen
 */
public class HuffTreeParser
{
    /**
     * Reads the given byte array and creates an optimal Huffman tree, based on the frequency of
     * appearance of each byte in the array. A pseudo-EoF marker is included in the tree by default.
     *
     * @param bytes raw data as byte array
     * @return pointer to root node of resultant Huffman tree
     */
    public static HuffNode buildTree(byte[] bytes)
    {
        MinHeap<HuffNode> heap = new MinHeap<>();
        long[] freqs = countFrequencies(bytes);

        for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++)
            if (freqs[b + 128] > 0)
                heap.add(new HuffNode(
                        (byte) b,
                        freqs[b + 128]
                ));
        heap.add(new HuffNode());   // pseudo-EoF node

        while (heap.size() > 1)
            heap.add(new HuffNode(
                    heap.pop(),
                    heap.pop()
            ));

        return heap.pop();
    }

    /**
     * Stores the given Huffman tree to the given output stream using pre-order traversal, such that
     * internal nodes are encoded as 0, and leaf nodes as 1 followed by the byte value of that leaf.
     *
     * @param node pointer to root node of Huffman tree
     * @param io I/O wrapper holding the output stream to which tree is encoded
     * @throws IOException if there's an error writing to the output stream
     */
    public static void encodeTree(HuffNode node, BinaryIO io) throws IOException
    {
        if (node.isLeaf())
            io
                    .writeBit(true)
                    .writeByte(node.getValue());
        else
        {
            io.writeBit(false);

            encodeTree(node.getLeft(), io);
            encodeTree(node.getRight(), io);
        }
    }

    /**
     * Reads Huffman tree from given input stream, and returns pointer to root node of the tree.
     *
     * <p>Assumes that the immediate next sequence of bits in the input stream contains a Huffman
     * tree encoded in a specific manner, followed by the prefix code of the pseudo-EoF node.</p>
     *
     * @param io I/O wrapper holding the input stream from which tree is decoded
     * @return pointer to root node of decoded Huffman tree
     * @throws IOException if there's an error reading the input stream
     */
    public static HuffNode decodeTree(BinaryIO io) throws IOException
    {
        HuffNode root = decode(io);
        overwriteEoF(root, io);

        return root;
    }


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


    private static long[] countFrequencies(byte[] bytes)
    {
        long[] freqs = new long[256];

        for (byte b : bytes)
            freqs[b + 128]++;

        return freqs;
    }

    private static HuffNode decode(BinaryIO io) throws IOException
    {
        return io.readBit()
                ? new HuffNode(io.readByte(), 0)
                : new HuffNode(
                        decode(io),
                        decode(io)
                );
    }

    private static void overwriteEoF(HuffNode node, BinaryIO io) throws IOException
    {
        while (!node.isLeaf())
            node = io.readBit()
                    ? node.getRight()
                    : node.getLeft();

        node.setEoF();
    }
}
