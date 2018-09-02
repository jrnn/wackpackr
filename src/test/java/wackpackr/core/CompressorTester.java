package wackpackr.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import wackpackr.util.ByteString;
import wackpackr.web.CompressionResult;

/**
 * This class contains a few reusable procedures for testing {@link Compressor} implementations.
 *
 * @author Juho Juurinen
 */
public class CompressorTester
{
    private final String[] ss = {
            "Appilan pappilan apupapin papupata pankolla kiehuu ja kuohuu. Appilan pappilan piski, paksuposki pisti apupapin papupadan poskeensa.",
            "Never gonna give you up, never gonna let you down, never gonna run around and desert you. Never gonna make you cry, never gonna say goodbye, never gonna tell a lie and hurt you.",
            "Now this is a story all about how my life got flipped turned upside down, and I'd like to take a minute, just sit right there, I'll tell you how I became the prince of a town called Bel-Air.",
            "Good morning, Paul. What will your first sequence of the day be? Computer, load up Celery Man please. Yes, Paul. Could you kick up the 4D3D3D3? 4D3D3D3 engaged. Add sequence: OYSTER.",
            "Father Pierre, why did you stay on in this colonial Campari-land, where the clink of glasses mingles with the murmur of a million mosquitoes, where waterfalls and whiskey wash away the worries of a world-weary whicker, where gin and tonics jingle in a gyroscopic jubilee of something beginning with J?"
    };
    private final Compressor compressor;

    public CompressorTester(Compressor compressor)
    {
        if (compressor == null)
            throw new NullPointerException();

        this.compressor = compressor;
    }

    public boolean compressesAsExpected(int i, byte[] input) throws IOException
    {
        return Arrays.equals(
                input,
                compressor.compress(ss[i].getBytes())
        );
    }

    public boolean decompressesAsExpected(int i, byte[] input) throws IOException
    {
        return Arrays.equals(
                ss[i].getBytes(),
                compressor.decompress(input)
        );
    }

    public boolean performsWithText() throws IOException
    {
        File f = new File("src/test/java/wackpackr/test.txt");
        byte[] input = Files.readAllBytes(f.toPath());

        return passesPerformanceTests(input, "text");
    }

    public boolean performsWithImage() throws IOException
    {
        File f = new File("src/test/java/wackpackr/test.bmp");
        byte[] input = Files.readAllBytes(f.toPath());

        return passesPerformanceTests(input, "image");
    }

    public boolean performsWithRandom() throws IOException
    {
        byte[] input = new byte[1024 * 1024];
        ThreadLocalRandom.current().nextBytes(input);

        return passesPerformanceTests(input, "random");
    }

    private boolean passesPerformanceTests(byte[] input, String type) throws IOException
    {
        ByteString bs = new ByteString(input);

        for (int kb = 64; kb <= 1024; kb += 64)
        {
            int size = 1024 * kb;

            while (bs.size() <= size)
                bs.append(input);

            CompressionResult res = new CompressionResult(
                    bs.getBytes(0, size),
                    type,
                    compressor
            );
            System.out.println(res);

            if (!res.isIntact())
                return false;
        }

        return true;
    }
}
