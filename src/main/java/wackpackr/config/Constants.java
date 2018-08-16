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
}
