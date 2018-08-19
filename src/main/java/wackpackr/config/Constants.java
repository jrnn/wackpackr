package wackpackr.config;

public class Constants
{
    /**
     * 32-bit identifier placed at the head of Huffman compressed files.
     */
    public static final long HUFFMAN_TAG = 0x07031986;

    /**
     * 32-bit identifier placed at the head of LZSS compressed files.
     */
    public static final long LZSS_TAG = 0x07072017;

    /**
     * 32-bit identifier placed at the head of LZW compressed files.
     */
    public static final long LZW_TAG = 0x04092009;

    /**
     * Minimum length for a back reference to be encoded in LZSS. Because pointers themselves take
     * two bytes, it makes no sense to write back references below three bytes length.
     */
    public static final int LZSS_THRESHOLD_LENGTH = 3;

    /**
     * In LZSS pointers, 12 bits are reserved for offset, for a possible range of [0, 4095]: so,
     * pointers can refer back no further than 4095 positions. "Zero- offset" is used as EoF marker.
     */
    public static final int LZSS_PREFIX_SIZE = 4095;

    /**
     * In LZSS pointers, 4 bits are reserved for length, for a possible range of [0, 15]: however,
     * because pointers are written only above a certain threshold, the range shifts up accordingly
     * (for instance, [3, 18]).
     */
    public static final int LZSS_BUFFER_SIZE = 15 + LZSS_THRESHOLD_LENGTH;

    /**
     * Size of sliding windows used in LZSS compression and decompression. Simply enough, the window
     * needs to accommodate at once both the prefix and lookahead buffer.
     */
    public static final int LZSS_WINDOW_SIZE = LZSS_PREFIX_SIZE + LZSS_BUFFER_SIZE;

    /**
     * Number of buckets to allocate for hash table that keeps track of three-byte occurrences in
     * the prefix window during LZSS encoding. Table size optimally is a prime number not close to
     * any power of two, and load factor preferably is in the 0.65~0.75 range. Since there will be a
     * maximum of ~4100 elements in the hash table at any one time, the picked value (6151) meets
     * the above conditions.
     */
    public static final int LZSS_BUCKETS = 6151;
}
