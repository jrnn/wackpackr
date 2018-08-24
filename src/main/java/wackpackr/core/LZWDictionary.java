package wackpackr.core;

import wackpackr.util.ByteString;
import wackpackr.util.LZWNode;

/**
 * Helper class that handles the dictionary needed in LZW compression and decompression.
 *
 * <p>The dictionary works a bit differently depending whether encoding or decoding. When encoding,
 * the dictionary needs to be able to tell quickly whether it contains a certain byte sequence or
 * not, and what is its index. When decoding, the dictionary needs to be able to fetch byte
 * sequences quickly by their index. Due to these differences, different data structures are
 * employed in each case.</p>
 *
 * <p>The first case is more complex. This implementation uses a trie structure, combined with a
 * trick that treats byte sequences as "prefix index—last byte" pairs (this takes advantage of the
 * fact that when a new byte sequence is put to the dictionary, its prefix must have been put there
 * earlier and thus has an index). With this trick, during the encoding process, the existence of
 * byte sequences can be tested in near-constant time. Also, since a byte sequence of any length is
 * stored simply as a pair of values, memory overhead is cut significantly.</p>
 *
 * <p>The decoding case is much simpler. The decoder only requests byte sequences by their index,
 * and never asks for a sequence that would not have been stored prior. It should be immediately
 * obvious that a standard array handles this just fine.</p>
 *
 * <p>Beside the dictionary, this class also keeps tabs on the bit size needed in encoding and
 * decoding, because this is directly a function of the running dictionary size (that is, how many
 * bits are needed to express the "highest" index in the dictionary at any given time). The bit size
 * is checked and communicated back to the encoder/decoder after each insertion.</p>
 *
 * <p>The bit size used in encoding is capped, which sets a limit for dictionary size. Whenever the
 * dictionary is full, it must be flushed to its initial state. Because this works a bit differently
 * whether encoding or decoding, monitoring dictionary size and flushing it is delegated to the
 * encoder/decoder.</p>
 *
 * @author Juho Juurinen
 */
public class LZWDictionary
{
    private static final int CODEWORD_BITSIZE = 16;
    private static final int MAX_DICTIONARY_SIZE = 1 << CODEWORD_BITSIZE;

    private int bitsize;
    private int dictMax;
    private int index;
    private ByteString[] dict;
    private LZWNode[] trie;

    /**
     * Constructs and initialises a new dictionary for LZW encoding or decoding.
     */
    public LZWDictionary()
    {
        reset();
    }

    /**
     * Returns {@code true} if and only if the dictionary holds the maximum allowed number of
     * elements.
     *
     * @return true if the dictionary is full
     */
    public boolean isFull()
    {
        return (index == MAX_DICTIONARY_SIZE);
    }

    /**
     * Returns the byte sequence associated with the given index.
     *
     * @param index zero-based dictionary index
     * @return byte sequence associated with the given index
     */
    public ByteString get(int index)
    {
        return dict[index];
    }

    /**
     * Inserts the given byte sequence to the dictionary, associating it with the next free index
     * in line.
     *
     * @param bs byte sequence to insert to dictionary
     * @return bit size needed in decoding after insertion
     */
    public int put(ByteString bs)
    {
        dict[index++] = bs;

        if (index > dictMax - 1)
        {
            bitsize++;
            dictMax <<= 1;
        }

        return bitsize;
    }

    /**
     * Returns the dictionary index associated with the byte sequence that is equal to "byte
     * sequence associated with the given prefix index, appended with the given byte value". If no
     * such sequence exists in the dictionary, a negative index is returned instead.
     *
     * @param prefix index of the byte sequence that forms the prefix part in the sought byte
     *        sequence
     * @param value the last byte in the sought byte sequence
     * @return index of the sought byte sequence if it exists; -1 otherwise
     */
    public int get(int prefix, byte value)
    {
        if (prefix < 0)
            return value + 129;

        LZWNode node = trie[prefix].get(value);
        return (node == null)
                ? -1
                : node.index();
    }

    /**
     * Inserts to the dictionary a byte sequence that is equal to "byte sequence associated with the
     * given prefix index, appended with the given byte value", and associates it with the next free
     * index in line.
     *
     * @param prefix index of the byte sequence that forms the prefix part in the byte sequence to
     *        insert
     * @param value the last byte in the byte sequence to insert
     * @return bit size needed in encoding after insertion
     */
    public int put(int prefix, byte value)
    {
        LZWNode node = new LZWNode(index, value);

        trie[prefix].insert(node);
        trie[index++] = node;

        if (index > dictMax)
        {
            bitsize++;
            dictMax <<= 1;
        }

        return bitsize;
    }

    /**
     * Flushes the dictionary to its initial state, with the zero index reserved for the pseudo-EoF
     * marker, and all possible one-byte sequences in the next 256 indexes, in ascending order.
     */
    public final void reset()
    {
        dict = new ByteString[MAX_DICTIONARY_SIZE];
        trie = new LZWNode[MAX_DICTIONARY_SIZE];

        for (index = 1; index < 257; index++)
        {
            byte b = (byte) (index - 129);

            dict[index] = new ByteString(b);
            trie[index] = new LZWNode(index, b);
        }

        bitsize = 9;
        dictMax = 1 << bitsize;
    }
}
