package wackpackr.web;

import java.io.IOException;
import java.util.Arrays;
import wackpackr.core.Compressor;

public class CompressionResult
{
    private final String compressors;
    private final String filetype;
    private Integer initialSize, compressedSize;
    private Long compressionTime, decompressionTime;
    private boolean isIntact;

    public CompressionResult(byte[] initial, String filetype, Compressor... compressors)
    {
        this.compressors = joinNames(compressors);
        this.filetype = filetype;

        run(initial, compressors);
    }

    public CompressionResult(byte[] initial, Compressor... compressors)
    {
        this(initial, "n/a", compressors);
    }

    public String getName()
    {
        return compressors;
    }

    public String getType()
    {
        return filetype;
    }

    public long getCompressionTime()
    {
        return compressionTime / 1_000_000;
    }

    public long getDecompressionTime()
    {
        return decompressionTime / 1_000_000;
    }

    public double getCompressionRate()
    {
        return 100.0 * compressedSize / initialSize;
    }

    public boolean isIntact()
    {
        return isIntact;
    }

    @Override
    public String toString()
    {
        return getName() + "\t" + getType() + "\t" + initialSize + "\t" + compressionTime + "\t"
                + decompressionTime + "\t" + getCompressionRate() + "\t" + isIntact();
    }



    private void run(byte[] initial, Compressor... compressors)
    {
        checkParameters(initial, compressors);

        this.initialSize = initial.length;
        try
        {
            byte[] compressed = compress(initial, compressors);
            byte[] decompressed = decompress(compressed, compressors);
            this.isIntact = Arrays.equals(initial, decompressed);
        }
        catch (Exception e)
        {
            this.isIntact = false;
        }
    }

    private byte[] compress(byte[] bs, Compressor... cs) throws IOException
    {
        long start = System.nanoTime();

        for (Compressor c : cs)
            bs = c.compress(bs);

        this.compressionTime = System.nanoTime() - start;
        this.compressedSize = bs.length;

        return bs;
    }

    private byte[] decompress(byte[] bs, Compressor... cs) throws IOException
    {
        long start = System.nanoTime();

        for (int i = cs.length - 1; i >= 0; i--)
            bs = cs[i].decompress(bs);

        this.decompressionTime = System.nanoTime() - start;
        return bs;
    }

    private void checkParameters(byte[] bs, Compressor... cs)
    {
        if (bs == null || bs.length < 1)
            throw new IllegalArgumentException("What am I supposed to compress?");

        if (cs == null || cs.length < 1)
            throw new IllegalArgumentException("What am I supposed to compress with?");

        for (Compressor c : cs)
            if (c == null)
                throw new IllegalArgumentException("How do you expect a null pointer to do compression?");
    }

    private String joinNames(Compressor... cs)
    {
        String s = "";
        int n = cs.length - 1;

        for (int i = 0; i < n; i++)
            s = s + cs[i].getName() + " > ";

        s += cs[n].getName();
        return s;
    }
}
