package wackpackr.core;

import java.util.Map;
import java.util.TreeMap;
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
    private final Map<Codeword, Integer> codewords = new TreeMap<>();

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
        return codewords.getOrDefault(new Codeword(i, b), -1);
    }

    public void put(ByteString bs)
    {
        bytestrings[bsIndex++] = bs;

        if (bsIndex == MAX_DICTIONARY_SIZE - 2)
            initBs();
    }

    public void put(int i, byte b)
    {
        codewords.put(new Codeword(i, b), cwIndex++);

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
        codewords.clear();

        for (cwIndex = 0; cwIndex < 256; cwIndex++)
            codewords.put(
                    new Codeword(-1, (byte) (cwIndex - 128)),
                    cwIndex
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
}
