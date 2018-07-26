package wackpackr.compressor;

import java.nio.charset.StandardCharsets;
import java.util.PriorityQueue;
import wackpackr.io.MockBitStream;
import wackpackr.util.HuffNode;

/**
 * First-cut implementation of Huffman algorithm that can only translate Strings to Huffman code.
 * Decompression not yet supported. The tree is stored at head of compressed binary, but without any
 * metadata or other identifiers to help in unpacking.
 *
 * @author Juho Juurinen
 */
public class Huffman
{
    public static String compress(String input)
    {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        // calculate frequencies
        long[] frequencies = new long[256];
        for (byte b : bytes)
            frequencies[b + 128]++;

        // create a leaf node for each encountered byte, and throw it in the heap
        PriorityQueue<HuffNode> heap = new PriorityQueue<>();
        for (int i = 0; i < 256; i++)
            if (frequencies[i] > 0)
                heap.offer(new HuffNode(i, frequencies[i]));

        // form Huffman tree
        while (heap.size() > 1)
            heap.offer(new HuffNode(
                    heap.poll(),
                    heap.poll()
            ));
        HuffNode root = heap.poll();

        // build translation table (byte <--> Huffman code) by traversing the tree
        String[] codes = new String[256];
        writeCodes(codes, root, "");

        // write tree at head of compressed binary
        MockBitStream bs = new MockBitStream("");
        writeTree(root, bs);

        // finally, translate input to compressed form
        for (byte b : bytes)
            bs.write(codes[b + 128]);

        // return compressed binary, which now includes both tree and data
        return bs.toString();
    }

    private static void writeCodes(String[] codes, HuffNode node, String code)
    {
        if (node.isLeaf())
            codes[node.value] = code;
        else
        {
            writeCodes(codes, node.left, code + "0");
            writeCodes(codes, node.right, code + "1");
        }
    }

    private static void writeTree(HuffNode node, MockBitStream bs)
    {
        if (node.isLeaf())
            bs.write("1" + Integer.toBinaryString(node.value + 256).substring(1));
        else
        {
            bs.write("0");
            writeTree(node.left, bs);
            writeTree(node.right, bs);
        }
    }

    private static HuffNode readTree(MockBitStream bs)
    {
        if (bs.nextBit() == 1)
            return new HuffNode(bs.nextByte(), 0);
        else
            return new HuffNode(readTree(bs), readTree(bs));
    }
}
