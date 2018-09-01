package wackpackr.web;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import wackpackr.core.Compressor;
import wackpackr.core.HuffCompressor;
import wackpackr.core.LZSSCompressor;
import wackpackr.core.LZWCompressor;

@Service
public class CompressionService
{
    private final List<CompressionResult> results = new ArrayList<>();

    public List<CompressionResult> getResults()
    {
        return results;
    }

    public void runTests(byte[] bytes)
    {
        results.clear();
        for (Compressor[] c : getCompressors())
            results.add(new CompressionResult(bytes, c));
    }

    private Compressor[][] getCompressors()
    {
        Compressor huff = new HuffCompressor();
        Compressor lzss = new LZSSCompressor();
        Compressor lzw = new LZWCompressor();

        return new Compressor[][]{
            { huff }, { lzss }, { lzw },
            { huff, lzss }, { huff, lzw },
            { lzss, huff }, { lzss, lzw },
            { lzw, huff }, { lzw, lzss },
        };
    }
}
