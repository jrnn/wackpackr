package wackpackr.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;

public class LZSSTest
{
    private final String s1_initial = "Appilan pappilan apupapin papupata pankolla kiehuu ja kuohuu. Appilan pappilan piski, paksuposki pisti apupapin papupadan poskeensa.";
    private final byte[] s1_compressed = {7, 7, 32, 23, 32, -100, 14, 6, -109, 97, -124, -36, 32, 56, 24, 96, 18, -122, 19, -127, -42, 1, -128, 105, -128, -111, 64, 40, 14, -122, 24, 4, -127, -72, -42, 111, 54, 27, 12, 34, 3, 89, -92, -54, 104, 58, -99, 68, 6, -88, 5, 1, -44, -33, 0, -96, 23, 8, 32, 125, -57, 3, 73, -52, -42, 105, 22, 64, 64, 13, 103, 51, -87, -64, -33, 0, -64, -128, -112, -99, 13, 48, 86, -45, 36, 10, -58, 3, -32, 101, 50, -101, -114, 102, 17, 116, 0, 0};
    private final String s2_initial = "Never gonna give you up, never gonna let you down, never gonna run around and desert you. Never gonna make you cry, never gonna say goodbye, never gonna tell a lie and hurt you.";
    private final byte[] s2_compressed = {7, 7, 32, 23, 39, 25, 78, -58, 83, -112, -128, -50, 111, 55, 27, -116, 34, 3, 57, -92, -20, 101, 16, 30, 77, -25, 81, 1, -44, -32, 44, 16, 27, -96, 51, 6, -61, 41, -46, 3, 4, 100, 55, -99, -51, -48, 26, -77, -111, -44, -36, 32, 48, -100, -115, -25, 83, 113, -112, 64, 97, -128, 32, 25, 12, -89, 51, 41, -54, 5, -124, 46, 16, 65, 106, 77, -90, 19, 92, 22, -116, -58, 114, 60, -63, 6, -50, 102, 19, -52, 2, -128, -34, 100, 49, 30, 76, -80, 25, -77, -95, -108, -40, 108, 16, 65, -20, 13, 38, 88, 45, 17, -96, -21, 5, -124, -128, 0, 0};

    @Test
    public void LZSSCompressionWorks() throws IOException
    {
        Assert.assertArrayEquals(
                LZSS.compress(s1_initial.getBytes()),
                s1_compressed
        );
        Assert.assertArrayEquals(
                LZSS.compress(s2_initial.getBytes()),
                s2_compressed
        );
    }

    @Test
    public void LZSSDecompressionWorks() throws IOException
    {
        Assert.assertEquals(
                s1_initial,
                new String(
                        LZSS.decompress(s1_compressed),
                        StandardCharsets.UTF_8
                )
        );
        Assert.assertEquals(
                s2_initial,
                new String(
                        LZSS.decompress(s2_compressed),
                        StandardCharsets.UTF_8
                )
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfIncorrectTagInHeader() throws IOException
    {
        byte[] invalid = new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        LZSS.decompress(invalid);
    }
}
