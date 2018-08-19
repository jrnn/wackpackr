package wackpackr.core;

import java.io.EOFException;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class LZSSCompressorTest
{
    private final Compressor lzss = new LZSSCompressor();
    private final CompressorTester tester = new CompressorTester(lzss);

    private final String[] ss = {
            "Appilan pappilan apupapin papupata pankolla kiehuu ja kuohuu. Appilan pappilan piski, paksuposki pisti apupapin papupadan poskeensa.",
            "Never gonna give you up, never gonna let you down, never gonna run around and desert you. Never gonna make you cry, never gonna say goodbye, never gonna tell a lie and hurt you.",
            "Now this is a story all about how my life got flipped turned upside down, and I'd like to take a minute, just sit right there, I'll tell you how I became the prince of a town called Bel-Air.",
            "Good morning, Paul. What will your first sequence of the day be? Computer, load up Celery Man please. Yes, Paul. Could you kick up the 4D3D3D3? 4D3D3D3 engaged. Add sequence: OYSTER.",
            "Father Pierre, why did you stay on in this colonial Campari-land, where the clink of glasses mingles with the murmur of a million mosquitoes, where waterfalls and whiskey wash away the worries of a world-weary whicker, where gin and tonics jingle in a gyroscopic jubilee of something beginning with J?"
    };
    private final byte[][] bs = {
            { 7, 7, 32, 23, 32, -100, 14, 6, -109, 97, -124, -36, 32, 56, 24, 96, 18, -122, 19, -127, -42, 1, -128, 105, -128, -111, 64, 40, 14, -122, 24, 4, -127, -72, -42, 111, 54, 27, 12, 34, 3, 89, -92, -54, 104, 58, -99, 68, 6, -88, 5, 1, -44, -33, 0, -96, 23, 8, 32, 125, -57, 3, 73, -52, -42, 105, 22, 64, 64, 13, 103, 51, -87, -64, -33, 0, -64, -128, -112, -99, 13, 48, 86, -45, 36, 10, -58, 3, -32, 101, 50, -101, -114, 102, 17, 116, 0, 0 },
            { 7, 7, 32, 23, 39, 25, 78, -58, 83, -112, -128, -50, 111, 55, 27, -116, 34, 3, 57, -92, -20, 101, 16, 30, 77, -25, 81, 1, -44, -32, 44, 16, 27, -96, 51, 6, -61, 41, -46, 3, 4, 100, 55, -99, -51, -48, 26, -77, -111, -44, -36, 32, 48, -100, -115, -25, 83, 113, -112, 64, 97, -128, 32, 25, 12, -89, 51, 41, -54, 5, -124, 46, 16, 65, 106, 77, -90, 19, 92, 22, -116, -58, 114, 60, -63, 6, -50, 102, 19, -52, 2, -128, -34, 100, 49, 30, 76, -80, 25, -77, -95, -108, -40, 108, 16, 65, -20, 13, 38, 88, 45, 17, -96, -21, 5, -124, -128, 0, 0 },
            { 7, 7, 32, 23, 39, 27, -50, -30, 3, -95, -96, -46, 115, 16, 64, 12, 12, 34, 3, -103, -48, -34, 114, 60, -120, 12, 38, -61, 96, -128, -62, 98, 55, -99, 78, -126, 3, 68, 7, -128, -38, 121, 16, 27, 13, 38, 99, 40, -128, -50, 111, 58, 8, 12, -58, -61, 73, -64, -32, 101, 50, 8, 14, -121, 83, -111, -70, 0, -32, 117, 56, 28, -51, 38, 67, 40, -128, -56, 111, 59, -101, -123, -126, 3, 9, -72, -56, 32, 36, -119, -52, -112, 45, 3, 89, -108, 64, 116, 55, -120, 14, -122, 24, 4, 1, -124, 64, 109, 52, -101, -114, -89, 67, 40, -80, 64, 106, 58, -100, -50, -126, 3, -103, -92, -24, 32, 57, 26, 76, -26, -125, -92, 29, 0, -54, 114, -128, -80, 18, 68, -16, 108, 3, -95, -106, 0, -96, 121, 55, -99, 96, -34, 68, -111, 1, -120, -54, 99, 48, -101, 96, -122, 6, -125, 40, -128, -32, 114, 52, -101, -116, 102, 81, 1, -68, -51, 4, -112, 58, 65, -104, 4, 6, 56, 78, 4, 30, 64, -124, 101, 54, 11, 72, 38, -109, -112, -70, 0, 0 },
            { 7, 7, 32, 23, 35, -101, -51, -26, 65, 1, -76, -34, 114, 55, 26, 77, -58, 113, 96, -128, -96, 97, 58, -101, 5, -62, 2, -71, -96, -62, 116, 16, 29, -51, 38, -61, 96, -128, -14, 111, 58, -100, -124, 6, 99, 73, -56, -26, 116, 16, 28, -52, -89, 19, -87, -108, -36, 99, 50, -120, 13, -26, 97, 1, -48, -48, 101, 16, 25, 12, 39, -111, 1, -120, -54, 63, 16, 16, -51, -26, -45, -127, -44, -24, 101, 57, 11, 4, 6, -61, 121, -124, -56, 32, 58, -100, 4, 4, 51, 41, -80, -54, 114, 60, -120, 9, -90, 19, 112, -128, -32, 108, 50, -104, 78, 102, 81, 112, -128, -78, 101, 57, -63, 117, 72, 102, -13, -87, -80, -55, 5, -111, 16, 26, -51, 38, 51, 92, 12, 6, 9, -62, 52, 34, 12, -32, 4, 35, -8, 4, -88, -128, -54, 110, 51, -104, 76, -26, 83, 32, -72, 64, 65, 50, 25, 32, -8, -61, -95, 1, 60, -78, 83, 42, 17, 74, 66, -24, 0, 0 },
            { 7, 7, 32, 23, 35, 24, 78, -122, -125, 41, -56, 64, 80, 52, -103, 78, 71, 35, 40, -80, 64, 119, 52, 30, 68, 6, 67, 73, -112, 64, 121, 55, -99, 68, 7, 51, -95, -124, -14, 32, 55, -101, -124, 6, -109, 112, -128, -24, 104, 52, -100, -60, 6, 51, 121, -80, -34, 110, 52, -104, 77, -126, 2, 25, -124, -38, 112, 48, -100, -115, 34, -45, 97, -124, -36, 100, -127, -104, -103, 78, 70, 88, 17, 1, -108, 64, 99, 54, 26, 77, -58, -79, 1, -68, -52, 32, 51, -101, 12, 39, 51, -103, -108, -26, 32, 54, -102, 77, -58, 115, 100, 2, 0, -18, 105, 58, 26, 32, 68, 70, -45, -87, -54, 0, 97, 2, 49, 48, -64, 116, 13, -122, -61, 76, 23, -64, -38, 111, 57, -100, 78, -90, -109, -95, -68, -54, 115, -126, 98, -99, -52, 39, 67, 41, -56, -52, 97, 54, 27, 14, 98, 8, 49, 4, 5, 64, -46, 115, 53, -103, 79, 48, 23, 3, -103, -96, 64, 97, 59, -62, 84, 32, -106, 39, 115, 121, -56, -28, 105, -126, -32, 65, 48, -96, 26, 6, -61, 32, -76, -18, 101, 48, -100, -96, 78, 6, -125, 73, -116, -42, 101, 57, 65, 53, 76, -16, -65, 8, 35, 9, -46, 23, -128, 99, 57, -120, 13, 80, -109, 40, 106, 9, -124, 64, 103, 60, -100, -115, -25, 51, 25, -68, -32, 105, 49, -120, 13, 71, 83, 17, -92, -40, 101, 50, -63, 56, 78, 102, -13, 105, -106, 30, 0, 110, 51, -120, 12, 70, 88, 30, -127, -70, 1, 67, 12, 18, 37, 15, -32, 0, 0 }
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
        lzss.decompress(invalid);
    }

    @Test(expected = EOFException.class)
    public void throwsExceptionIfNoEoFMarker() throws IOException
    {
        byte[] invalid = new byte[]{ 7, 7, 32, 23, 32, -100, 14, 6, -109, 97, -124, -36, 32, 56, 24, 96, 18, -122, 19, -127, -42, 1, -128, 105, -128, -111, 64, 40, 14 };
        lzss.decompress(invalid);
    }

    @Test
    public void compressorKnowsItsName()
    {
        Assert.assertEquals("LZSS", lzss.getName());
    }
}
