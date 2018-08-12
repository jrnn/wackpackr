package wackpackr.core;

import wackpackr.config.Constants;
import wackpackr.util.ChainedHashTable;
import wackpackr.util.SlidingWindow;

/**
 * Helper class that handles the "sliding window" dictionary needed in LZSS compression and
 * decompression. Needs to be initialised a bit differently depending whether compressing or
 * decompressing, which is handled with a separate constructor for each case.
 *
 * The decisive thing here is the technique used in longest match search, which practically alone
 * determines compression efficiency. Current implementation tries to reproduce a technique used
 * e.g. in Deflate: recurring three-byte sequences are memorised as they flow in and out of the
 * prefix window, so that searching can be limited only to positions where at least the first three
 * bytes match (which incidentally is also the threshold length for encoding a pointer). A hash
 * table is used to associate three-byte sequences to positions, allowing insertion and search in
 * constant time.
 *
 * @author Juho Juurinen
 */
public class LZSSWindowOperator
{
    private ChainedHashTable<Integer> positions;
    private final SlidingWindow<Byte> window = new SlidingWindow(Constants.LZSS_WINDOW_SIZE);

    /**
     * Constructor used for decoding purposes, when the hash table for pattern matching is not
     * needed (and hence not initialised). A "dummy byte" is inserted at head of the window, just so
     * that the read pointer can move one step ahead of the decoded stream (otherwise would throw
     * {@code IndexOutOfBoundsException}).
     */
    public LZSSWindowOperator()
    {
        window.insert(null);
    }

    /**
     * Constructor used for encoding purposes: hash table used in memorising pattern recurrences is
     * initialised, and the initial lookahead buffer is pushed into the window.
     *
     * There will be maximum ~4100 elements stored in the hash table at any one time. Load factor
     * should be in the 0.65~0.75 range, while table size should be a prime number midway between
     * two powers of two. With these border conditions, an optimal size is 6151.
     *
     * @param initialBuffer lookahead buffer at beginning of decoding
     */
    public LZSSWindowOperator(byte[] initialBuffer)
    {
        positions = new ChainedHashTable(6151);

        for (byte b : initialBuffer)
            window.insert(b);
    }

    /**
     * Reads backwards through current prefix window, searching for longest partial or complete
     * match of current buffer. Returns the length and offset (relative to sliding window cursor
     * position) of the best match, or [0, 0] if no matches were found. In case of ties, the match
     * with least distance from buffer is returned. Also, if a complete match is found, the search
     * terminates since there is no point in looking any further.
     *
     * @return length and offset of longest match, as integer array
     */
    public int[] findLongestMatch()
    {
        if (window.read(3) == null)
            return new int[]{ 0, 0 };

        int maxLength = 0;
        int maxOffset = 0;
        Object[] ps = positions.getValues(
                window.read(0),
                window.read(1),
                window.read(2)
        );

        for (Object p : ps)
        {
            int length = 0;
            int offset = window.cursor() - (int) p;

            while (length < Constants.LZSS_BUFFER_SIZE)
            {
                if (window.read(length) == null)
                    break;

                if (!window.read(length - offset).equals(window.read(length)))
                    break;

                length++;
            }
            if (maxLength < length)
            {
                maxLength = length;
                maxOffset = offset;
            }
            if (maxLength == Constants.LZSS_BUFFER_SIZE)
                break;
        }

        return new int[]{ maxLength, maxOffset };
    }

    /**
     * Moves the window forward one step. The given byte is inserted to the head of the buffer,
     * which in turn pushes the last byte in buffer to the head of the prefix window. Further, if
     * the prefix is full, the byte at the end is dumped.
     *
     * This method also handles the recording of positions of new three-byte sequences as they first
     * enter the prefix window, as well as their deletion when they eventually are dumped out from
     * the other end. At first glance, the deletion part looks problematic due to hash collisions:
     * how to be sure the correct value is deleted, when virtually any byte sequence may associate
     * to it? However, this is not an issue, because the byte sequences are encountered in the exact
     * same order at both ends of the prefix window; and, in particular, the hash table used for
     * holding the values stores them in sequential order in case of hash collisions.
     *
     * @param b byte to insert at head of window
     */
    public void slideForward(Byte b)
    {
        Byte out = window.insert(b);

        if (out != null)
        {
            positions.deleteOldest(
                    out,
                    window.read(-Constants.LZSS_PREFIX_SIZE + 1),
                    window.read(-Constants.LZSS_PREFIX_SIZE + 2)
            );
        }

        if (window.read(2) != null)
            positions.insert(
                    window.cursor(),
                    window.read(0),
                    window.read(1),
                    window.read(2)
            );

        window.move();
    }

    /**
     * Reads and returns the byte value coming out next from the lookahead buffer.
     *
     * @return next byte to come out from the lookahead buffer
     */
    public Byte next()
    {
        return window.read();
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

        window.insert(b);
        window.move();

        return b;
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
}
