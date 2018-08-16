package wackpackr.core;

import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.junit.Assert;
import org.junit.Test;

public class LZWTest
{
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
            byte[] initial = input.getBytes(UTF_8);
            byte[] compressed = LZW.compress(initial);
            Assert.assertArrayEquals(initial, LZW.decompress(compressed));
        }
    }
}
