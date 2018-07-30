package wackpackr.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import wackpackr.io.BinaryIO;
import wackpackr.util.HuffNode;
import wackpackr.util.MinHeap;

/**
 * Simplistic implementation of Huffman algorithm. The tree storing the bit mappings is encoded at
 * head of compressed binary.
 *
 * Class is quite bloated with some fugly big methods. Needs some serious restructuring.
 *
 * @author Juho Juurinen
 */
public class Huffman
{
    public static byte[] compress(byte[] input) throws Exception
    {
        // calculate frequencies
        long[] frequencies = new long[256];
        for (byte b : input)
            frequencies[b + 128]++;

        // create a leaf node for each encountered byte, and throw it in the heap
        MinHeap<HuffNode> heap = new MinHeap<>();
        for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++)
            if (frequencies[b + 128] > 0)
                heap.add(new HuffNode((byte) b, frequencies[b + 128]));

        // add still node for pseudo-eof character
        heap.add(new HuffNode());

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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryIO io = new BinaryIO(out);
        writeTag(io);

        // write tree at head of compressed binary
        writeTree(root, io);

        // append path to pseudo-eof node after encoded tree
        encode(codes[256], io);

        // read input once more, and translate it to compressed form
        for (byte b : input)
            encode(codes[b + 128], io);

        // finally, explicitly add pseudo-eof at the end + pad with zeroes (temporary, I hope...)
        encode(codes[256], io);
        io.writeByte(0);

        byte[] bytes = out.toByteArray();
        io.close();

        // return compressed binary, which now includes both tree and data
        return bytes;
    }

    public static byte[] decompress(byte[] input) throws Exception
    {
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryIO io = new BinaryIO(in, out);

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
                        ? node.getRight()
                        : node.getLeft();

            if (node.isEoF())
                break;

            io.writeByte(node.getValue());
        }

        byte[] bytes = out.toByteArray();
        io.close();  // can be replaced with smarter code organization? (try-with block)

        return bytes;
    }


    /* --- Most of the stuff below this line probably belongs in separate classes? --- */


    private static void encode(String code, BinaryIO io) throws Exception
    {
        for (char c : code.toCharArray())
        {
            assert c == '0' || c == '1';
            io.writeBit(c == '1');
        }
    }

    private static void writeCodes(String[] codes, HuffNode node, String code)
    {
        if (node.isEoF())
            codes[256] = code;
        else if (node.isLeaf())
            codes[node.getValue() + 128] = code;
        else
        {
            writeCodes(codes, node.getLeft(), code + "0");
            writeCodes(codes, node.getRight(), code + "1");
        }
    }

    private static void writeTree(HuffNode node, BinaryIO io) throws Exception
    {
        if (node.isLeaf())
        {
            io.writeBit(true);
            io.writeByte(node.getValue());
        }
        else
        {
            io.writeBit(false);
            writeTree(node.getLeft(), io);
            writeTree(node.getRight(), io);
        }
    }

    private static HuffNode readTree(BinaryIO io) throws Exception
    {
        return io.readBit()
                ? new HuffNode((byte) io.readByte(), 0)
                : new HuffNode(readTree(io), readTree(io));
    }

    private static void overwriteEof(HuffNode node, BinaryIO io) throws Exception
    {
        while (!node.isLeaf())
            node = io.readBit()
                    ? node.getRight()
                    : node.getLeft();

        node.setEoF();
    }

    private static final long TAG = 0x07031986;

    private static void writeTag(BinaryIO io) throws Exception
    {
        io.writeLong(TAG);
    }

    private static boolean checkTag(BinaryIO io) throws Exception
    {
        long tag = io.readLong();
        return (tag == TAG);
    }
}
