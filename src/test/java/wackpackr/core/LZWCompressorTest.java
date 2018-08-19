package wackpackr.core;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class LZWCompressorTest
{
    private final Compressor lzw = new LZWCompressor();

    @Test
    public void test() throws IOException
    {
        String[] inputs = {
            "Appilan pappilan apupapin papupata pankolla kiehuu ja kuohuu. Appilan pappilan piski, paksuposki pisti apupapin papupadan poskeensa.",
            "Never gonna give you up, never gonna let you down, never gonna run around and desert you. Never gonna make you cry, never gonna say goodbye, never gonna tell a lie and hurt you.",
            "Anyone who feels that if so many more students whom we haven’t actually admitted are sitting in on the course than ones we have that the room had to be changed, then probably auditors will have to be excluded, is likely to agree that the curriculum needs revision."
        };

        for (String input : inputs)
        {
            byte[] initial = input.getBytes();
            byte[] compressed = lzw.compress(initial);
            Assert.assertArrayEquals(initial, lzw.decompress(compressed));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfIncorrectTagInHeader() throws IOException
    {
        byte[] invalid = new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        lzw.decompress(invalid);
    }

    @Test
    public void compressorKnowsItsName()
    {
        Assert.assertEquals("LZW", lzw.getName());
    }
}
