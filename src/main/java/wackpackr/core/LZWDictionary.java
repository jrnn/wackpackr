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

    private int bitSize;
    private int dictMax;

    private int index;
    private ByteString[] dict;
    private Node[] trie;

    public LZWDictionary()
    {
        init();
    }

    public void resetIfFull()
    {
        if (index == MAX_DICTIONARY_SIZE)
            init();
    }

    public ByteString get(int index)
    {
        return dict[index];
    }

    public int get(int prefix, byte value)
    {
        return (prefix < 0)
                ? value + 129
                : trie[prefix].getIndex(value);
    }

    public int put(ByteString bs)
    {
        dict[index++] = bs;

        if (index > dictMax - 1)
        {
            bitSize++;
            dictMax <<= 1;
        }
        resetIfFull();

        return bitSize;
    }

    public int put(int prefix, byte value)
    {
        Node node = new Node(index, value);

        trie[prefix].insert(node);
        trie[index++] = node;

        if (index > dictMax)
        {
            bitSize++;
            dictMax <<= 1;
        }

        return bitSize;
    }


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


    private void init()
    {
        dict = new ByteString[MAX_DICTIONARY_SIZE];
        trie = new Node[MAX_DICTIONARY_SIZE];

        for (index = 1; index < 257; index++)
        {
            byte b = (byte) (index - 129);

            dict[index] = new ByteString(b);
            trie[index] = new Node(index, b);
        }

        bitSize = 9;
        dictMax = 1 << bitSize;
    }

    private static final class Node
    {
        final int index;
        final byte value;
        Node next = null, left = null, right = null;

        Node(int index, byte value)
        {
            this.index = index;
            this.value = value;
        }

        public int getIndex(byte value)
        {
            Node node = search(value, next);

            return (node == null)
                    ? -1
                    : node.index;
        }

        public void insert(Node node)
        {
            if (next == null)
                next = node;
            else
                insert(node, next);
        }

        private Node search(byte value, Node root)
        {
            if (root == null)
                return null;

            if (value == root.value)
                return root;

            root = (value < root.value)
                    ? root.left
                    : root.right;

            return search(value, root);
        }

        private void insert(Node node, Node root)
        {
            if (node.value < root.value)
                if (root.left == null)
                    root.left = node;
                else
                    insert(node, root.left);
            else
                if (root.right == null)
                    root.right = node;
                else
                    insert(node, root.right);
        }
    }
}
