package wackpackr.core;

import java.util.ArrayDeque;
import java.util.Iterator;
import wackpackr.config.Constants;
import wackpackr.util.SlidingWindow;

/**
 * Helper class that handles the "sliding window" dictionary needed in LZSS compression and
 * decompression. The decisive thing here is the technique used in longest match search, which
 * practically alone determines compression efficiency. The current implementation is naive: the
 * positions where different byte values appear in the prefix window are memorised as they come and
 * go, so that searching can be limited only to positions where at least the initial byte matches.
 * This possibly will be replaced with something better, later.
 *
 * @author Juho Juurinen
 */
public class LZSSWindowOperator
{
    private ArrayDeque<Integer>[] positions;
    private final SlidingWindow<Byte> window = new SlidingWindow(Constants.LZSS_WINDOW_SIZE);

    /**
     * Reads backwards through current prefix window, searching for longest partial or complete
     * match of current buffer. Returns the length and offset (relative to sliding window cursor
     * position) of the best match, or [0, 0] if no matches were found. In case of ties, the match
     * with least distance from buffer is returned. Also, if a complete match is found, the search
     * terminates since there is no point in looking any further.
     *
     * @return length and offset of longest match (as integer array)
     */
    public int[] findLongestMatch()
    {
        int maxLength = 0;
        int maxOffset = 0;
        Iterator<Integer> iter = positions[window.read() + 128].descendingIterator();

        while (iter.hasNext())
        {
            int length = 0;
            int offset = window.cursor() - iter.next();

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
     * Moves the window forward one step. In effect, the given byte is inserted to the head of the
     * buffer, which pushes the last byte in buffer to the head of the prefix window. Further, if
     * the prefix is full, the byte at the end is dumped.
     *
     * @param b byte to insert at head of window
     */
    public void slideForward(Byte b)
    {
        Byte out = window.insert(b);

        if (out != null)
            positions[out + 128].poll();

        positions[window.read() + 128].offer(window.cursor());
        window.move();
    }

    public void initForEncoding(Byte[] initialBuffer)
    {
        positions = new ArrayDeque[256];

        for (int i = 0; i < 256; i++)
            positions[i] = new ArrayDeque<>();

        for (Byte b : initialBuffer)
            window.insert(b);
    }

    public Byte next()
    {
        return window.read();
    }

    /**
     * Copy back reference to head of window. Used when decoding back references.
     *
     * @param offset
     * @return
     */
    public Byte copyBackReference(int offset)
    {
        Byte b = window.read(-offset);

        window.insert(b);
        window.move();

        return b;
    }

    public void insertAndMove(Byte b)
    {
        window.insert(b);
        window.move();
    }
}
