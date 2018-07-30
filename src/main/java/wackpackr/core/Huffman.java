package wackpackr.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import wackpackr.io.BinaryIO;
import wackpackr.util.HuffNode;

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
        // read through input and build huffman tree
        HuffNode root = HuffTreeParser.buildTree(input);

        // build translation table (byte <--> Huffman code) by traversing the tree
        String[] codes = new String[257];
        writeCodes(codes, root, "");

        // initialize new binary stream with "ID tag" at head position
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryIO io = new BinaryIO(out);
        writeTag(io);

        // write tree at head of compressed binary
        HuffTreeParser.encodeTree(root, io);

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

        // decode Huffman tree from header
        HuffNode root = HuffTreeParser.decodeTree(io);

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
