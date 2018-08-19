package wackpackr.core;

import java.io.EOFException;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class LZWCompressorTest
{
    private final Compressor lzw = new LZWCompressor();
    private final CompressorTester tester = new CompressorTester(lzw);

    private final String[] ss = {
            "Appilan pappilan apupapin papupata pankolla kiehuu ja kuohuu. Appilan pappilan piski, paksuposki pisti apupapin papupadan poskeensa.",
            "Never gonna give you up, never gonna let you down, never gonna run around and desert you. Never gonna make you cry, never gonna say goodbye, never gonna tell a lie and hurt you.",
            "Now this is a story all about how my life got flipped turned upside down, and I'd like to take a minute, just sit right there, I'll tell you how I became the prince of a town called Bel-Air.",
            "Good morning, Paul. What will your first sequence of the day be? Computer, load up Celery Man please. Yes, Paul. Could you kick up the 4D3D3D3? 4D3D3D3 engaged. Add sequence: OYSTER.",
            "Father Pierre, why did you stay on in this colonial Campari-land, where the clink of glasses mingles with the murmur of a million mosquitoes, where waterfalls and whiskey wash away the worries of a world-weary whicker, where gin and tonics jingle in a gyroscopic jubilee of something beginning with J?"
    };
    private final byte[][] bs = {
        { 4, 9, 32, 9, 0, -63, 0, -16, 0, -16, 0, -23, 0, -20, 0, -31, 0, -18, 0, -96, 0, -16, 0, -31, 1, 1, 1, 3, 1, 5, 0, -96, 1, 9, 0, -11, 1, 8, 1, 2, 1, 6, 1, 16, 1, 15, 0, -31, 0, -12, 0, -31, 1, 7, 1, 5, 0, -21, 0, -17, 0, -20, 1, 4, 0, -96, 0, -21, 0, -23, 0, -27, 0, -24, 0, -11, 0, -11, 0, -96, 0, -22, 1, 23, 0, -21, 0, -11, 0, -17, 1, 34, 0, -11, 0, -82, 0, -96, 1, 0, 1, 2, 1, 4, 1, 18, 1, 9, 1, 48, 1, 12, 1, 2, 0, -13, 1, 31, 0, -84, 1, 24, 0, -21, 0, -13, 1, 15, 0, -17, 1, 55, 0, -23, 1, 7, 0, -23, 0, -13, 0, -12, 1, 64, 1, 14, 1, 16, 0, -23, 1, 50, 0, -16, 1, 20, 0, -28, 1, 53, 1, 62, 0, -21, 0, -27, 0, -27, 0, -18, 0, -13, 0, -31, 0, -82, -1, -1, 0 },
        { 4, 9, 32, 9, 0, -50, 0, -27, 0, -10, 0, -27, 0, -14, 0, -96, 0, -25, 0, -17, 0, -18, 0, -18, 0, -31, 1, 5, 0, -23, 1, 2, 0, -96, 0, -7, 0, -17, 0, -11, 0, -96, 0, -11, 0, -16, 0, -84, 0, -96, 0, -18, 1, 1, 1, 3, 1, 5, 1, 7, 1, 9, 0, -96, 0, -20, 0, -27, 0, -12, 1, 14, 1, 16, 0, -96, 0, -28, 0, -17, 0, -9, 0, -18, 1, 21, 1, 23, 1, 2, 1, 4, 1, 6, 1, 8, 1, 10, 0, -14, 0, -11, 0, -18, 0, -96, 0, -31, 0, -14, 1, 16, 0, -18, 0, -28, 1, 50, 1, 54, 1, 35, 0, -27, 0, -13, 1, 3, 1, 32, 1, 15, 0, -11, 0, -82, 0, -96, 1, 0, 1, 42, 1, 26, 1, 45, 0, -96, 0, -19, 0, -31, 0, -21, 0, -27, 1, 33, 1, 17, 0, -29, 0, -14, 0, -7, 1, 40, 1, 24, 1, 43, 1, 27, 1, 10, 0, -13, 0, -31, 0, -7, 1, 26, 0, -17, 0, -28, 0, -30, 0, -7, 0, -27, 1, 81, 1, 68, 1, 44, 1, 28, 0, -12, 0, -27, 0, -20, 0, -20, 1, 50, 1, 29, 0, -23, 1, 75, 0, -31, 1, 57, 0, -24, 0, -11, 0, -14, 1, 62, 1, 16, 0, -82, -1, -1, 0 },
        { 4, 9, 32, 9, 0, -50, 0, -17, 0, -9, 0, -96, 0, -12, 0, -24, 0, -23, 0, -13, 0, -96, 1, 6, 0, -96, 0, -31, 0, -96, 0, -13, 0, -12, 0, -17, 0, -14, 0, -7, 1, 10, 0, -20, 0, -20, 1, 10, 0, -30, 0, -17, 0, -11, 0, -12, 0, -96, 0, -24, 1, 1, 0, -96, 0, -19, 1, 17, 0, -20, 0, -23, 0, -26, 0, -27, 0, -96, 0, -25, 0, -17, 1, 25, 0, -26, 1, 32, 0, -16, 0, -16, 0, -27, 0, -28, 1, 3, 0, -11, 0, -14, 0, -18, 1, 44, 0, -96, 0, -11, 0, -16, 0, -13, 0, -23, 0, -28, 1, 35, 0, -28, 1, 1, 0, -18, 0, -84, 1, 10, 0, -18, 1, 45, 0, -55, 0, -89, 1, 45, 1, 32, 0, -21, 1, 35, 1, 14, 1, 3, 0, -31, 1, 69, 1, 10, 1, 29, 0, -23, 0, -18, 1, 24, 0, -27, 1, 61, 0, -22, 0, -11, 1, 13, 1, 12, 0, -23, 1, 25, 0, -14, 0, -23, 0, -25, 0, -24, 1, 25, 1, 4, 0, -27, 0, -14, 1, 80, 0, -96, 1, 65, 1, 19, 1, 3, 0, -27, 1, 99, 0, -7, 1, 23, 1, 26, 1, 28, 0, -55, 0, -96, 0, -30, 0, -27, 0, -29, 0, -31, 0, -19, 1, 70, 0, -24, 1, 35, 0, -16, 1, 88, 0, -18, 0, -29, 1, 35, 0, -17, 0, -26, 1, 75, 1, 14, 0, -9, 0, -18, 0, -96, 1, 111, 1, 19, 1, 50, 0, -62, 1, 101, 0, -83, 0, -63, 0, -23, 0, -14, 0, -82, -1, -1, 0 },
        { 4, 9, 32, 9, 0, -57, 0, -17, 0, -17, 0, -28, 0, -96, 0, -19, 0, -17, 0, -14, 0, -18, 0, -23, 0, -18, 0, -25, 0, -84, 0, -96, 0, -48, 0, -31, 0, -11, 0, -20, 0, -82, 0, -96, 0, -41, 0, -24, 0, -31, 0, -12, 0, -96, 0, -9, 0, -23, 0, -20, 0, -20, 0, -96, 0, -7, 0, -17, 0, -11, 0, -14, 0, -96, 0, -26, 0, -23, 0, -14, 0, -13, 1, 23, 0, -13, 0, -27, 0, -15, 0, -11, 0, -27, 0, -18, 0, -29, 0, -27, 0, -96, 0, -17, 0, -26, 0, -96, 0, -12, 0, -24, 1, 47, 0, -28, 0, -31, 0, -7, 0, -96, 0, -30, 0, -27, 0, -65, 0, -96, 0, -61, 0, -17, 0, -19, 0, -16, 0, -11, 0, -12, 0, -27, 0, -14, 1, 12, 0, -20, 0, -17, 0, -31, 1, 3, 0, -11, 0, -16, 1, 62, 0, -27, 0, -20, 1, 69, 1, 57, 0, -51, 0, -31, 0, -18, 0, -96, 0, -16, 1, 80, 0, -31, 1, 40, 1, 18, 0, -39, 0, -27, 0, -13, 1, 12, 1, 14, 1, 16, 1, 18, 1, 63, 1, 16, 1, 3, 1, 30, 0, -11, 0, -96, 0, -21, 0, -23, 0, -29, 0, -21, 0, -96, 1, 76, 1, 51, 1, 53, 0, -96, 0, -76, 0, -60, 0, -77, 1, 115, 1, 115, 1, 61, 1, 114, 1, 116, 1, 121, 0, -96, 1, 44, 0, -25, 0, -31, 0, -25, 0, -27, 0, -28, 1, 18, 0, -63, 0, -28, 1, 3, 1, 40, 1, 42, 1, 44, 1, 46, 0, -70, 0, -96, 0, -49, 0, -39, 0, -45, 0, -44, 0, -59, 0, -46, 0, -82, -1, -1, 0 },
        { 4, 9, 32, 9, 0, -58, 0, -31, 0, -12, 0, -24, 0, -27, 0, -14, 0, -96, 0, -48, 0, -23, 1, 4, 0, -14, 0, -27, 0, -84, 0, -96, 0, -9, 0, -24, 0, -7, 0, -96, 0, -28, 0, -23, 0, -28, 0, -96, 0, -7, 0, -17, 0, -11, 0, -96, 0, -13, 0, -12, 0, -31, 1, 16, 0, -17, 0, -18, 0, -96, 0, -23, 1, 31, 1, 2, 0, -23, 0, -13, 0, -96, 0, -29, 0, -17, 0, -20, 1, 30, 0, -23, 0, -31, 0, -20, 0, -96, 0, -61, 0, -31, 0, -19, 0, -16, 0, -31, 0, -14, 0, -23, 0, -83, 0, -20, 0, -31, 0, -18, 0, -28, 1, 12, 1, 14, 1, 4, 0, -27, 0, -96, 1, 2, 1, 62, 0, -29, 0, -20, 1, 33, 0, -21, 0, -96, 0, -17, 0, -26, 0, -96, 0, -25, 1, 55, 0, -13, 0, -13, 0, -27, 1, 37, 0, -19, 1, 33, 1, 74, 1, 78, 1, 13, 0, -23, 1, 2, 1, 63, 1, 3, 0, -96, 0, -19, 0, -11, 0, -14, 1, 90, 1, 5, 1, 71, 0, -96, 0, -31, 1, 89, 0, -23, 0, -20, 1, 67, 1, 30, 1, 89, 0, -17, 0, -13, 0, -15, 0, -11, 1, 85, 0, -17, 1, 78, 1, 59, 1, 3, 1, 10, 1, 13, 1, 1, 1, 4, 0, -26, 1, 44, 0, -20, 1, 37, 1, 56, 1, 20, 1, 14, 1, 36, 0, -21, 0, -27, 1, 16, 0, -9, 0, -31, 0, -13, 0, -24, 1, 96, 1, -128, 1, 16, 1, 64, 1, 13, 0, -17, 0, -14, 1, 52, 1, 83, 1, 95, 1, 97, 0, -9, 1, -119, 0, -20, 0, -28, 0, -83, 0, -9, 0, -27, 1, 51, 1, 127, 0, -24, 0, -23, 0, -29, 1, 125, 0, -14, 1, 111, 1, 61, 1, 73, 1, 33, 1, 96, 1, 57, 1, 63, 1, 42, 0, -29, 1, 37, 0, -22, 1, 81, 0, -20, 1, 62, 1, -96, 1, 97, 0, -25, 0, -7, 0, -14, 1, 104, 1, 39, 0, -16, 1, -103, 0, -96, 0, -22, 0, -11, 0, -30, 1, 99, 0, -27, 1, 62, 1, 95, 0, -13, 0, -17, 0, -19, 0, -27, 1, 35, 0, -18, 0, -25, 0, -96, 0, -30, 0, -27, 0, -25, 1, 33, 0, -18, 1, 81, 1, 84, 1, 86, 0, -54, 0, -65, -1, -1, 0 }
    };

    @Test
    public void compressionWorks() throws IOException
    {
        for (int i = 0; i < 5; i++)
            Assert.assertTrue(
                    tester.compressesAsExpected(
                            ss[i].getBytes(),
                            bs[i]
                    ));
    }

    @Test
    public void decompressionWorks() throws IOException
    {
        for (int i = 0; i < 5; i++)
            Assert.assertTrue(
                    tester.decompressesAsExpected(
                            bs[i],
                            ss[i].getBytes()
                    ));
    }

    @Test
    public void testPerformanceWithText() throws IOException
    {
        Assert.assertTrue(tester.performsWithText());
    }

    @Test
    public void testPerformanceWithImage() throws IOException
    {
        Assert.assertTrue(tester.performsWithImage());
    }

    @Test
    public void testPerformanceWithRandomBytes() throws IOException
    {
        Assert.assertTrue(tester.performsWithRandom());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfIncorrectTagInHeader() throws IOException
    {
        byte[] invalid = new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        lzw.decompress(invalid);
    }

    @Test(expected = EOFException.class)
    public void throwsExceptionIfNoEoFMarker() throws IOException
    {
        byte[] invalid = new byte[]{ 4, 9, 32, 9, 0, -63, 0, -16, 0, -16, 0, -23, 0, -20, 0, -31, 0, -18, 0, -96, 0, -16, 0, -31, 1, 1, 1, 3, 1, 5, 0, -96, 1, 9, 0 };
        lzw.decompress(invalid);
    }

    @Test
    public void compressorKnowsItsName()
    {
        Assert.assertEquals("LZW", lzw.getName());
    }
}
