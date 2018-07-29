package wackpackr.core;

import java.nio.charset.StandardCharsets;
import wackpackr.io.MockBitStream;
import wackpackr.util.HuffNode;
import wackpackr.util.MinHeap;

/**
 * First-cut implementation of Huffman algorithm that can only work on Strings. Decompression not
 * yet supported. The tree storing the bit mappings is encoded at head of compressed binary.
 *
 * Class is quite bloated with some fugly big methods. Needs some serious restructuring.
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
        MinHeap<HuffNode> heap = new MinHeap<>();
        for (int i = 0; i < 256; i++)
            if (frequencies[i] > 0)
                heap.add(new HuffNode(i, frequencies[i]));

        // add still node for pseudo-eof character
        heap.add(new HuffNode(256, 0));

        // form Huffman tree
        while (heap.size() > 1)
            heap.add(new HuffNode(
                    heap.pop(),
                    heap.pop()
            ));
        HuffNode root = heap.pop();

        // build translation table (byte <--> Huffman code) by traversing the tree
        String[] codes = new String[257];
        writeCodes(codes, root, "");

        // initialize new binary stream with "ID tag" at head position
        MockBitStream bs = new MockBitStream("");
        writeTag(bs);

        // write tree at head of compressed binary
        writeTree(root, bs);

        // append path to pseudo-eof node after encoded tree
        bs.write(codes[256]);

        // read input once more, and translate it to compressed form
        for (byte b : bytes)
            bs.write(codes[b + 128]);

        // finally, explicitly add pseudo-eof at the end
        bs.write(codes[256]);

        // return compressed binary, which now includes both tree and data
        return bs.toString();
    }


    /* --- Most of the stuff below this line probably belongs in separate classes? --- */


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
            bs.write("1" + Integer.toBinaryString(node.value % 256 + 256).substring(1));
        else
        {
            bs.write("0");
            writeTree(node.left, bs);
            writeTree(node.right, bs);
        }
    }

    public static HuffNode readTree(MockBitStream bs)
    {
        return (bs.nextBit() == 1)
                ? new HuffNode(bs.nextByte(), 0)
                : new HuffNode(readTree(bs), readTree(bs));
    }

    public static void overwriteEof(HuffNode node, MockBitStream bs)
    {
        while (!node.isLeaf())
            node = (bs.nextBit() == 0)
                    ? node.left
                    : node.right;

        node.value = 256;
    }

    private static final long TAG = 0x07031986;

    private static void writeTag(MockBitStream bs)
    {
        String s = Long.toBinaryString(TAG);
        while (s.length() < 32)
            s = "0" + s;
        bs.write(s);
    }

    public static boolean checkTag(MockBitStream bs)
    {
        long tag = bs.nextLong();
        return (tag == TAG);
    }
}
