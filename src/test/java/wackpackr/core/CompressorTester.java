package wackpackr.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import wackpackr.util.ByteString;

/**
 * This class contains a few reusable procedures for testing {@link Compressor} implementations.
 *
 * @author Juho Juurinen
 */
public class CompressorTester
{
    private final Compressor compressor;

    public CompressorTester(Compressor compressor)
    {
        if (compressor == null)
            throw new NullPointerException();

        this.compressor = compressor;
    }

    public boolean compressesAsExpected(byte[] input, byte[] expected) throws IOException
    {
        return Arrays.equals(
                expected,
                compressor.compress(input)
        );
    }

    public boolean decompressesAsExpected(byte[] input, byte[] expected) throws IOException
    {
        return Arrays.equals(
                expected,
                compressor.decompress(input)
        );
    }

    public boolean performsWithText() throws IOException
    {
        System.out.println("TESTING WITH A TEXT FILE");

        File f = new File("src/test/java/wackpackr/test.txt");
        byte[] input = Files.readAllBytes(f.toPath());

        return passesPerformanceTests(input);
    }

    public boolean performsWithImage() throws IOException
    {
        System.out.println("TESTING WITH AN IMAGE FILE");

        File f = new File("src/test/java/wackpackr/test.bmp");
        byte[] input = Files.readAllBytes(f.toPath());

        return passesPerformanceTests(input);
    }

    public boolean performsWithRandom() throws IOException
    {
        System.out.println("TESTING WITH A PSEUDORANDOM BYTESTREAM");

        byte[] input = new byte[1024 * 1024];
        ThreadLocalRandom.current().nextBytes(input);

        return passesPerformanceTests(input);
    }

    private boolean passesPerformanceTests(byte[] input) throws IOException
    {
        ByteString bs = new ByteString(input);
        System.out.println("filesize (bytes)\tcompression (ns)\tdecompression (ns)\trate\tintact?");

        for (int kb = 64; kb <= 1024; kb += 64)
        {
            int size = 1024 * kb;

            while (bs.size() <= size)
                bs.append(input);

            if (!testPerformance(bs.getBytes(0, size)))
                return false;
        }

        return true;
    }

    private boolean testPerformance(byte[] input) throws IOException
    {
        long start, end;
        System.out.print(input.length + "\t");

        start = System.nanoTime();
        byte[] compressed = compressor.compress(input);
        end = System.nanoTime();
        System.out.print((end - start) + "\t");

        start = System.nanoTime();
        byte[] decompressed = compressor.decompress(compressed);
        end = System.nanoTime();

        boolean success = Arrays.equals(input, decompressed);

        System.out.print((end - start) + "\t");
        System.out.println((1.0 * compressed.length / input.length) + "\t" + success);

        return success;
    }
}
