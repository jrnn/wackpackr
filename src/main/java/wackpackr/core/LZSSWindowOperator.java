package wackpackr.core;

import wackpackr.util.ErraticHashTable;
import wackpackr.util.SlidingWindow;

/**
 * Helper class that handles the "sliding window" dictionary needed in LZSS compression and
 * decompression.
 *
 * <p>There are separate constructors for compression vs. decompression purposes, because the window
 * needs to be initialised a bit differently in each case.</p>
 *
 * <p>The decisive thing here is the technique used in longest match search, which practically
 * alone determines compression efficiency. Current implementation tries to reproduce a technique
 * used in Deflate: recurring three-byte sequences are memorised as they flow in and out of the
 * prefix window, so that searching can be limited only to positions where at least the first three
 * bytes match (which incidentally is also the threshold length for encoding a pointer).</p>
 *
 * @author Juho Juurinen
 */
public class LZSSWindowOperator
{
    private final int bufferSize, prefixSize;
    private ErraticHashTable<Integer> positions;
    private final SlidingWindow<Byte> window;

    /**
     * Constructs a new sliding window operator with a lighter configuration for decoding purposes.
     * More specifically, the hash table (which is only needed for pattern matching) is not
     * initialised.
     *
     * <p>A dummy byte is inserted at head of the window, just so that the read pointer can move
     * one step ahead of the decoded stream.</p>
     *
     * @param bufferSize maximum number of bytes held in lookahead buffer at once
     * @param prefixSize maximum number of bytes held in prefix at once
     */
    public LZSSWindowOperator(int bufferSize, int prefixSize)
    {
        this.bufferSize = bufferSize;
        this.prefixSize = prefixSize;
        this.window = new SlidingWindow<>(bufferSize + prefixSize);

        window.insert(null);
    }

    /**
     * Constructs a new sliding window operator for encoding purposes. The hash table used in
     * memorising pattern recurrences is initialised, and the initial lookahead buffer is pushed
     * into the window.
     *
     * Hash table size optimally is a prime number not close to any power of two, and load factor
     * preferably is in the 0.65~0.75 range. Since there will be a maximum of ~4100 elements in the
     * table at any one time, one good choice that meets the above conditions is 6151.
     *
     * @param bufferSize maximum number of bytes held in lookahead buffer at once
     * @param prefixSize maximum number of bytes held in prefix at once
     * @param initialBuffer lookahead buffer at beginning of encoding
     */
    public LZSSWindowOperator(int bufferSize, int prefixSize, byte[] initialBuffer)
    {
        this.bufferSize = bufferSize;
        this.prefixSize = prefixSize;
        this.window = new SlidingWindow<>(bufferSize + prefixSize);
        this.positions = new ErraticHashTable<>(6151);

        for (byte b : initialBuffer)
            window.insert(b);
    }

    /**
     * Reads backwards through current prefix window, searching for longest partial or complete
     * match of current lookahead buffer. Returns the length and offset, relative to sliding window
     * cursor position, of the best match; or [0, 0] if no matches are found.
     *
     * <p>In case of ties, the match with least distance from buffer is returned. Also, if a
     * complete match is found, the search terminates since there is no point in looking any
     * further.</p>
     *
     * @return length and offset of longest match, as integer array
     */
    public int[] findLongestMatch()
    {
        int maxLength = 0, maxOffset = 0;

        if (window.read(3) != null)
            for (Object p : positions.get(
                    window.read(0),
                    window.read(1),
                    window.read(2)).toArrayReverse())
            {
                int length = 0, offset = window.cursor() - (int) p;

                for (; length < bufferSize; length++)
                    if (window.read(length) == null
                            || !window.read(length - offset).equals(window.read(length)))
                        break;

                if (maxLength < length)
                {
                    maxLength = length;
                    maxOffset = offset;
                }
                if (maxLength == bufferSize)
                    break;
            }

        return new int[]{ maxLength, maxOffset };
    }

    /**
     * Moves the window forward one step, inserting the given byte to the head of the buffer and
     * dumping the byte at the other end, if the prefix is full.
     *
     * <p>This method also handles the recording of positions of new three-byte sequences as they
     * first enter the prefix window, as well as their deletion when they eventually drop out from
     * the other end.</p>
     *
     * <p>At first glance, the deletion part looks problematic due to hash collisions: how to be
     * sure the correct value is deleted, when virtually any byte sequence may associate to it?
     * However, this is not an issue, because the byte sequences are encountered in the exact same
     * order at both ends of the prefix window; and, in particular, the hash table used for holding
     * the values stores them in sequential order in case of hash collisions.</p>
     *
     * @param b byte to insert at head of window
     */
    public void slideForward(Byte b)
    {
        Byte out = window.insert(b);

        if (out != null)
            positions.get(
                    out,
                    window.read(-prefixSize + 1),
                    window.read(-prefixSize + 2)
            ).removeFirst();

        if (window.read(2) != null)
            positions.put(
                    window.cursor(),
                    window.read(0),
                    window.read(1),
                    window.read(2)
            );

        window.move();
    }

    /**
     * Reads, but does not remove, the byte value coming out next from the lookahead buffer.
     *
     * @return next byte to come out from the lookahead buffer
     */
    public Byte peek()
    {
        return window.read();
    }

    /**
     * Inserts given byte at head of the window, and moves the read pointer forward one step.
     *
     * @param b byte to insert at head of the window
     */
    public void insertAndMove(Byte b)
    {
        window.insert(b);
        window.move();
    }

    /**
     * Copies byte value at given offset from the read pointer to head of the window. Used when
     * decoding back references. Also, returns the copied byte value.
     *
     * @param offset number of positions to jump over from current cursor position
     * @return byte value read and copied at given offset
     */
    public Byte copyBackReference(int offset)
    {
        Byte b = window.read(-offset);
        insertAndMove(b);

        return b;
    }
}
