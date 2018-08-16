package wackpackr.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;

public class HuffmanTest
{
    private final String s1_initial = "Appilan pappilan apupapin papupata pankolla kiehuu ja kuohuu. Appilan pappilan piski, paksuposki pisti apupapin papupadan poskeensa.";
    private final byte[] s1_compressed = {7, 3, 25, -122, 22, -103, 5, -64, 90, -59, -88, -128, 75, 44, -110, -27, -94, -126, -73, 45, -21, -91, -107, 97, 93, 87, 59, 100, -110, 117, 31, -44, 92, -93, -6, -114, 121, -56, -94, -25, -99, 118, 46, -88, -73, -1, -116, 11, -26, -18, 50, 49, -114, -76, -35, -46, -103, -44, 127, 81, 114, -113, -22, 40, -12, 9, 41, 116, 123, -101, 122, 1, 71, -82, 7, 60, -28, 81, 115, -50, -109, -44, 91, 122, 47, -66, -67, -91, -110, 0};
    private final String s2_initial = "Never gonna give you up, never gonna let you down, never gonna run around and desert you. Never gonna make you cry, never gonna say goodbye, never gonna tell a lie and hurt you.";
    private final byte[] s2_compressed = {7, 3, 25, -122, 36, 5, -44, 91, 109, 107, 69, -34, -55, 101, 45, -53, -107, -124, 46, 114, -53, -75, -68, -68, -82, -106, -63, 105, 16, 11, 21, -114, -32, -105, 83, -84, -2, 71, -73, -105, 71, -10, 75, 63, -29, 44, -26, -96, -98, 120, -111, -27, -47, -3, -110, -50, -33, 71, 53, 5, -22, -71, -119, 30, 93, 31, -39, 44, -92, -123, -42, -92, 89, 113, 98, -34, 14, -70, 57, -87, -24, -10, -14, -24, -2, -55, 98, -117, 82, -50, 106, 30, 90, -26, 36, 121, 116, 127, 100, -77, 11, -31, -3, -43, -7, 60, 120, -111, -27, -47, -3, -110, -50, -97, 125, -106, 119, -31, -105, 22, 42, 74, -24, -26, -89, -81, 32};

    @Test
    public void huffmanCompressionWorks() throws IOException
    {
        Assert.assertArrayEquals(
                HuffCompressor.compress(s1_initial.getBytes()),
                s1_compressed
        );
        Assert.assertArrayEquals(
                HuffCompressor.compress(s2_initial.getBytes()),
                s2_compressed
        );
    }

    @Test
    public void huffmanDecompressionWorks() throws IOException
    {
        Assert.assertEquals(
                s1_initial,
                new String(
                        HuffCompressor.decompress(s1_compressed),
                        StandardCharsets.UTF_8
                )
        );
        Assert.assertEquals(
                s2_initial,
                new String(
                        HuffCompressor.decompress(s2_compressed),
                        StandardCharsets.UTF_8
                )
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfIncorrectTagInHeader() throws IOException
    {
        byte[] invalid = new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        HuffCompressor.decompress(invalid);
    }
}
