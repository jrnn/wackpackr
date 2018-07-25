package wackpackr.compressor;

import java.nio.charset.StandardCharsets;
import java.util.PriorityQueue;
import wackpackr.util.HuffNode;

/**
 * First-cut implementation of Huffman algorithm that can only translate Strings to Huffman code.
 * Byproduct Huffman tree is not stored anywhere. Cannot yet do decompression.
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
        HuffNode left, right;
        while (heap.size() > 1)
        {
            left = heap.poll();
            right = heap.poll();
            heap.offer(new HuffNode(left, right));
        }

        // build translation table (byte <--> Huffman code) by traversing the tree
        String[] codes = new String[256];
        writeCodes(codes, heap.poll(), "");

        // finally, translate input to compressed form
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(codes[b + 128]);

        return sb.toString();
    }

    private static void writeCodes(String[] codes, HuffNode node, String code)
    {
        if (node == null)
            return;
        if (node.isLeaf())
            codes[node.value] = code;
        else
        {
            writeCodes(codes, node.left, code + "0");
            writeCodes(codes, node.right, code + "1");
        }
    }
}
