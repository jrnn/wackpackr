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

    private int index;
    private ByteString[] dict;
    private Node[] trie;

    public LZWDictionary()
    {
        init();
    }

    public ByteString get(int index)
    {
        return dict[index];
    }

    public int get(int prefix, byte value)
    {
        return (prefix < 0)
                ? value + 128
                : trie[prefix].getIndex(value);
    }

    public void put(ByteString bs)
    {
        dict[index++] = bs;

        if (index == MAX_DICTIONARY_SIZE - 2)
            init();
    }

    public void put(int prefix, byte value)
    {
        Node node = new Node(index, value);

        trie[prefix].insert(node);
        trie[index++] = node;

        if (index == MAX_DICTIONARY_SIZE - 2)
            init();
    }


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


    private void init()
    {
        dict = new ByteString[MAX_DICTIONARY_SIZE];
        trie = new Node[MAX_DICTIONARY_SIZE];

        for (index = 0; index < 256; index++)
        {
            byte b = (byte) (index - 128);

            dict[index] = new ByteString(b);
            trie[index] = new Node(index, b);
        }
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
