package wackpackr.core;

/**
 * Horrible quick-and-dirty LZ77 compressor, at this point only for personal learning purposes ...
 *
 * @author Juho Juurinen
 */
public class LZSS
{
    // sliding window has 4096 bytes at once; i.e. offset can be max 4096
    private static final int PREFIX_SIZE = 4096;
    // lookahead buffer has 16 bytes at once; i.e. length can be max 16
    // note that min length to encode should be 3. so, can encode 3 to 18 bytes length
    private static final int BUFFER_SIZE = 16;

    public static String compress()
    {
        String input = "Never gonna give you up, never gonna let you down, never gonna run around and desert you. Never gonna make you cry, never gonna say goodbye, never gonna tell a lie and hurt you.";
        String output = "";

        int pos = 0;
        while (pos < input.length())
        {
            int bestLength = 0;
            int bestOffset = 0;
            int maxOffset = Math.min(PREFIX_SIZE, pos);
            for (int offset = 1; offset <= maxOffset; offset++)
            {
                int length = 0;
                int bufferPos = pos;
                int prefixPos = pos - offset;
                int maxBufferPos = Math.min(pos + BUFFER_SIZE, input.length());
                while (bufferPos < maxBufferPos)
                {
                    if (input.charAt(bufferPos) != input.charAt(prefixPos))
                        break;
                    length++;
                    bufferPos++;
                    prefixPos++;
                }
                if (bestLength < length)
                {
                    bestLength = length;
                    bestOffset = offset;
                }
            }
            output += (bestLength > 2)
                    ? "(" + bestOffset + "," + bestLength + ")"
                    : input.charAt(pos);
            pos += (bestLength > 2)
                    ? bestLength
                    : 1;
        }
        return output;
    }
}
