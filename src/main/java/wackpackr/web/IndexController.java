package wackpackr.web;

import java.util.Arrays;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import wackpackr.core.Huffman;
import wackpackr.core.LZSS;

@Controller
public class IndexController
{
    private static String HUFF_COMPRESS = "n/a";
    private static String HUFF_RATE = "n/a";
    private static String HUFF_DECOMPRESS = "n/a";
    private static String HUFF_INTACT = "n/a";
    private static String LZSS_COMPRESS = "n/a";
    private static String LZSS_RATE = "n/a";
    private static String LZSS_DECOMPRESS = "n/a";
    private static String LZSS_INTACT = "n/a";

    @RequestMapping(value = "*", method = RequestMethod.GET)
    public String index(Model model)
    {
        model.addAttribute("huffCompress", HUFF_COMPRESS);
        model.addAttribute("huffRate", HUFF_RATE);
        model.addAttribute("huffDecompress", HUFF_DECOMPRESS);
        model.addAttribute("huffIntact", HUFF_INTACT);
        model.addAttribute("lzssCompress", LZSS_COMPRESS);
        model.addAttribute("lzssRate", LZSS_RATE);
        model.addAttribute("lzssDecompress", LZSS_DECOMPRESS);
        model.addAttribute("lzssIntact", LZSS_INTACT);

        return "index";
    }

    @RequestMapping(value = "/compress", method = RequestMethod.POST)
    public String upload(@RequestParam("file") MultipartFile file)
    {
        if (file == null || file.getSize() == 0)
            return "redirect:/";

        long start, end;
        byte[] compressed, decompressed, initial;

        try
        {
            initial = file.getBytes();

            start = System.nanoTime();
            compressed = Huffman.compress(initial);
            end = System.nanoTime();
            HUFF_COMPRESS = String.format("%,d", (end - start) / 1000000);
            HUFF_RATE = String.format("%.2f", (100.0 * compressed.length / initial.length));
            start = System.nanoTime();
            decompressed = Huffman.decompress(compressed);
            end = System.nanoTime();
            HUFF_DECOMPRESS = String.format("%,d", (end - start) / 1000000);
            HUFF_INTACT = (Arrays.equals(initial, decompressed)) ? "YES" : "NO";

            start = System.nanoTime();
            compressed = LZSS.compress(initial);
            end = System.nanoTime();
            LZSS_COMPRESS = String.format("%,d", (end - start) / 1000000);
            LZSS_RATE = String.format("%.2f", (100.0 * compressed.length / initial.length));
            start = System.nanoTime();
            decompressed = LZSS.decompress(compressed);
            end = System.nanoTime();
            LZSS_DECOMPRESS = String.format("%,d", (end - start) / 1000000);
            LZSS_INTACT = (Arrays.equals(initial, decompressed)) ? "YES" : "NO";
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "redirect:/";
    }
}
