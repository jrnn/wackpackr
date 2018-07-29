package wackpackr.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import wackpackr.io.BinaryIO;
import wackpackr.io.MockBitStream;
import wackpackr.util.HuffNode;
import wackpackr.util.MinHeap;

/**
 * First-cut implementation of Huffman algorithm that can only work on Strings. Next step is to make
 * this work directly with 1s and 0s. The tree storing the bit mappings is encoded at head of
 * compressed binary.
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

        // finally, explicitly add pseudo-eof at the end + pad with zeroes (temporary, I hope...)
        bs.write(codes[256]);
        while (bs.toString().length() % 8 != 0)
            bs.write("0");

        // return compressed binary, which now includes both tree and data
        return bs.toString();
    }

    public static String decompress(String input) throws Exception
    {
        byte[] bs = new BigInteger(input, 2).toByteArray(); // <--- this is ABSOLUTE BULLSHIT, only a temporary measure !!!
        InputStream is = new ByteArrayInputStream(bs);
        BinaryIO io = new BinaryIO();
        io.setInputStream(is);
        MockBitStream out = new MockBitStream("");

        // check 32-bit identifier at file head -- throw exception if mismatch
        if (!checkTag(io))
            throw new IOException("Fool, this ain't a Huffman compressed file");

        // decode Huffman tree from header, incl. pseudo-eof node path
        HuffNode root = Huffman.readTree(io);
        overwriteEof(root, io);

        // read input until eof marker, translating to decompressed form on the go
        HuffNode node;
        while (true)
        {
            node = root;
            while (!node.isLeaf())
                node = io.readBit()
                        ? node.right
                        : node.left;

            if (node.value == 256)  //  eof
                break;

            out.write(Integer.toBinaryString(node.value % 256 + 256).substring(1));
        }

        // convert decompressed binary back to byte array
        int n = out.toString().length() / 8;
        byte[] bytes = new byte[n];
        for (int i = 0; i < n; i++)
            bytes[i] = (byte) (out.nextByte() - 128);

        io.close();  // these can be replaced with smarter code organization
        is.close();  // (doing IO inside a try-with block)

        // return ASCII
        return new String(bytes, StandardCharsets.UTF_8);
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

    private static HuffNode readTree(BinaryIO io) throws Exception
    {
        return io.readBit()
                ? new HuffNode(io.readByte(), 0)
                : new HuffNode(readTree(io), readTree(io));
    }

    private static void overwriteEof(HuffNode node, BinaryIO io) throws Exception
    {
        while (!node.isLeaf())
            node = io.readBit()
                    ? node.right
                    : node.left;

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

    private static boolean checkTag(BinaryIO io) throws Exception
    {
        long tag = io.readLong();
        return (tag == TAG);
    }
}
