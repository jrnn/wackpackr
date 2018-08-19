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
        Node n = nodes[i].child;
        if (n == null)
            return -1;
        while (n != null)
        {
            if (n.value == b)
                return n.index;
            n = n.next;
        }
        return -1;
    }

    public void put(ByteString bs)
    {
        bytestrings[bsIndex++] = bs;

        if (bsIndex == MAX_DICTIONARY_SIZE - 2)
            initBs();
    }

    public void put(int i, byte b)
    {
        Node o = new Node(cwIndex, b);
        Node n = nodes[i];
        if (n.child == null)
            n.child = o;
        else
        {
            n = n.child;
            while (n.next != null)
                n = n.next;
            n.next = o;
        }
        nodes[cwIndex++] = o;
        //cwIndex++;

        if (cwIndex == MAX_DICTIONARY_SIZE - 2)
            initCw();
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

    private static final class Codeword implements Comparable<Codeword>
    {
        private final int index;
        private final byte value;

        Codeword(int index, byte value)
        {
            this.index = index;
            this.value = value;
        }

        @Override
        public int compareTo(Codeword o)
        {
            int i = Integer.compare(index, o.index);
            return (i != 0)
                    ? i
                    : Byte.compare(value, o.value);
        }
    }

    private static final class Node
    {
        final int index;
        final byte value;
        Node next = null, child = null;

        Node(int index, byte value)
        {
            this.index = index;
            this.value = value;
        }
    }
}
