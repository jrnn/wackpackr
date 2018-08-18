package wackpackr.core;

import java.io.EOFException;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class HuffCompressorTest
{
    private final Compressor huff = new HuffCompressor();
    private final CompressorTester tester = new CompressorTester(huff);

    private final String[] ss = {
            "Appilan pappilan apupapin papupata pankolla kiehuu ja kuohuu. Appilan pappilan piski, paksuposki pisti apupapin papupadan poskeensa.",
            "Never gonna give you up, never gonna let you down, never gonna run around and desert you. Never gonna make you cry, never gonna say goodbye, never gonna tell a lie and hurt you.",
            "Now this is a story all about how my life got flipped turned upside down, and I'd like to take a minute, just sit right there, I'll tell you how I became the prince of a town called Bel-Air.",
            "Good morning, Paul. What will your first sequence of the day be? Computer, load up Celery Man please. Yes, Paul. Could you kick up the 4D3D3D3? 4D3D3D3 engaged. Add sequence: OYSTER.",
            "Father Pierre, why did you stay on in this colonial Campari-land, where the clink of glasses mingles with the murmur of a million mosquitoes, where waterfalls and whiskey wash away the worries of a world-weary whicker, where gin and tonics jingle in a gyroscopic jubilee of something beginning with J?"
    };
    private final byte[][] bs = {
            { 7, 3, 25, -122, 22, -103, 5, -64, 90, -59, -88, -128, 75, 44, -110, -27, -94, -126, -73, 45, -21, -91, -107, 97, 93, 87, 59, 100, -110, 117, 31, -44, 92, -93, -6, -114, 121, -56, -94, -25, -99, 118, 46, -88, -73, -1, -116, 11, -26, -18, 50, 49, -114, -76, -35, -46, -103, -44, 127, 81, 114, -113, -22, 40, -12, 9, 41, 116, 123, -101, 122, 1, 71, -82, 7, 60, -28, 81, 115, -50, -109, -44, 91, 122, 47, -66, -67, -91, -110, 0 },
            { 7, 3, 25, -122, 36, 5, -44, 91, 109, 107, 69, -34, -55, 101, 45, -53, -107, -124, 46, 114, -53, -75, -68, -68, -82, -106, -63, 105, 16, 11, 21, -114, -32, -105, 83, -84, -2, 71, -73, -105, 71, -10, 75, 63, -29, 44, -26, -96, -98, 120, -111, -27, -47, -3, -110, -50, -33, 71, 53, 5, -22, -71, -119, 30, 93, 31, -39, 44, -92, -123, -42, -92, 89, 113, 98, -34, 14, -70, 57, -87, -24, -10, -14, -24, -2, -55, 98, -117, 82, -50, 106, 30, 90, -26, 36, 121, 116, 127, 100, -77, 11, -31, -3, -43, -7, 60, 120, -111, -27, -47, -3, -110, -50, -97, 125, -106, 119, -31, -105, 22, 42, 74, -24, -26, -89, -81, 32 },
            { 7, 3, 25, -122, 36, 5, -122, -34, 93, -53, 85, -98, -57, 105, 11, -83, -54, -40, 91, -106, 105, 98, -26, -92, -37, 69, -94, -55, 116, 89, 68, -98, -59, 112, 20, 42, 8, -128, 75, 101, -44, -27, -82, -13, -12, 125, -85, 13, -61, -40, 123, 8, 91, 86, 63, -60, -103, 19, -54, -61, 76, 43, 11, -1, -110, -11, 112, -43, 116, -86, 95, -33, 123, 38, -62, 52, -20, -112, -10, -49, -98, 50, -84, -91, 98, 83, 37, -34, 50, 75, -2, -29, 84, -44, -3, -62, 23, -67, 33, -67, 88, -46, 22, -46, -49, -92, 93, -82, 52, -36, 116, 122, -78, -17, 19, 38, -12, -55, -3, 96, 97, 88, 92, 121, -26, -46, -2, 55, 28, 123, 23, -93, 124, 45, 66, 26, -84, -96, -38, 76, -10, 79, -114, -97, -89, -27, -29, -11, -6, 0 },
            { 7, 3, 25, -122, 11, 98, -13, 112, -78, -106, -14, -37, 80, -71, 23, 75, -99, -44, 37, -55, 101, -115, 110, 80, -19, 2, -50, -86, 79, -26, 106, 37, -111, 32, 11, 50, -97, 82, -76, -105, 21, 78, -82, 80, 104, -85, 21, 29, 97, 20, -46, 1, 58, -69, -42, 86, -71, -89, -55, -34, -119, 124, -94, 46, 92, -54, -120, -78, -66, 112, -122, -21, -97, -52, -33, 62, 64, 48, -111, -41, -72, 114, -74, -77, 52, -12, 115, 40, -50, 78, 25, -109, -99, 127, 11, 119, 26, 122, 100, 80, 110, -62, -72, -80, 39, -81, -100, 122, 98, 5, 98, -33, 30, -106, 24, 31, 52, -61, 125, 45, -117, 43, -25, 8, 105, -111, -62, -8, 72, -5, -9, 49, -2, -50, 60, -55, -50, -1, 106, -38, -74, -83, 61, -2, -43, -75, 109, 92, 101, 71, -88, 55, -122, -20, -67, -13, 79, 71, 50, -116, -4, -69, -117, -17, 85, 46, -34, 56, 124, -128 },
            { 7, 3, 25, -122, 11, 97, 107, -106, 91, 87, 123, 65, 115, -71, 22, 113, 113, 32, 19, -6, 28, -75, 112, -79, 91, -123, -66, -62, 46, -74, 98, -44, -93, 41, 85, 11, 37, -107, 32, 90, 87, 69, -114, -14, -46, 85, 79, -114, -41, 85, -6, -43, 108, 92, 71, -2, -81, -85, -33, -59, 25, 61, 63, -24, 125, -49, -68, 124, -101, -24, 8, 126, -112, -51, 57, 27, 105, 94, 106, 18, -11, -117, -120, -19, 111, 120, -17, 124, 28, -30, 104, -89, 48, 37, 18, -45, 15, -99, -127, 105, -117, -68, 123, -57, 120, 116, 40, -24, 93, 20, -23, -61, -32, 14, -121, -61, -124, 104, -93, -67, 22, -126, -30, 59, 91, -59, 62, -75, -90, 64, 19, 75, -41, -120, -7, 4, -65, -15, 74, 30, -110, -97, -9, -114, -15, 66, -81, 90, 104, -89, 78, 40, 80, -83, -87, 92, -81, -8, -113, -66, 18, -44, 92, 71, 107, 121, -100, -6, 94, -67, -24, 126, -7, 53, 78, 118, 5, -18, 125, 57, -97, -84, 39, -48, -37, -66, -43, 40, 111, -63, 119, -94, -100, -112, 59, -15, -13, -77, 55, -74, 115, -65, 59, 49, 119, -113, 85, -102, 86, -112 }
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
        huff.decompress(invalid);
    }

    @Test(expected = EOFException.class)
    public void throwsExceptionIfNoEoFMarker() throws IOException
    {
        byte[] invalid = new byte[]{ 7, 3, 25, -122, 22, -103, 5, -64, 90, -59, -88, -128, 75, 44, -110, -27, -94, -126, -73, 45, -21, -91, -107, 97, 93, 87, 59, 100 };
        huff.decompress(invalid);
    }
}
