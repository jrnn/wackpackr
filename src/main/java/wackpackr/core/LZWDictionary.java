package wackpackr.core;

import wackpackr.util.ByteString;

/**
 * Dictionary for LZW, work in progress
 *
 * @author Juho Juurinen
 */
public class LZWDictionary
{
    private static final int CODEWORD_BITSIZE = 16;
    private static final int MAX_DICTIONARY_SIZE = 1 << CODEWORD_BITSIZE;

    private int bsIndex, cwIndex;
    private ByteString[] bytestrings;
    private Node[] nodes;

    public LZWDictionary()
    {
        initBs();
        initCw();
    }

    public ByteString get(int i)
    {
        return bytestrings[i];
    }

    public int get(int i, byte b)
    {
        if (i < 0)
            return b + 128;

        Node node = nodes[i];
        node = (b < node.value)
                ? node.childLeft
                : node.childRight;

        return search(node, b);
    }

    public void put(ByteString bs)
    {
        bytestrings[bsIndex++] = bs;

        if (bsIndex == MAX_DICTIONARY_SIZE - 2)
            initBs();
    }

    public void put(int i, byte b)
    {
        Node node = nodes[i];
        Node newNode = new Node(cwIndex, b);
        nodes[cwIndex++] = newNode;

        if (b < node.value)
            if (node.childLeft == null)
                node.childLeft = newNode;
            else
                insert(newNode, node.childLeft);
        else
            if (node.childRight == null)
                node.childRight = newNode;
            else
                insert(newNode, node.childRight);

        if (cwIndex == MAX_DICTIONARY_SIZE - 2)
            initCw();
    }

    private static final class Node
    {
        final int index;
        final byte value;
        Node nextLeft = null, nextRight = null;
        Node childLeft = null, childRight = null;

        Node(int index, byte value)
        {
            this.index = index;
            this.value = value;
        }
    }

    private void initBs()
    {
        bytestrings = new ByteString[MAX_DICTIONARY_SIZE];

        for (bsIndex = 0; bsIndex < 256; bsIndex++)
            bytestrings[bsIndex] = new ByteString((byte) (bsIndex - 128));
    }

    private void initCw()
    {
        nodes = new Node[MAX_DICTIONARY_SIZE];

        for (cwIndex = 0; cwIndex < 256; cwIndex++)
            nodes[cwIndex] = new Node(
                    cwIndex,
                    (byte) (cwIndex - 128)
            );
    }

    private int search(Node node, byte b)
    {
        if (node == null)
            return -1;

        if (b == node.value)
            return node.index;

        node = (b < node.value)
                ? node.nextLeft
                : node.nextRight;

        return search(node, b);
    }

    private void insert(Node newNode, Node node)
    {
        if (newNode.value < node.value)
            if (node.nextLeft == null)
                node.nextLeft = newNode;
            else
                insert(newNode, node.nextLeft);
        else
            if (node.nextRight == null)
                node.nextRight = newNode;
            else
                insert(newNode, node.nextRight);
    }
}
