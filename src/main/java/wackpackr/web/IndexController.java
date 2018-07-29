package wackpackr.web;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import wackpackr.core.Huffman;
import wackpackr.io.StringIO;

@Controller
public class IndexController
{
    private static String INPUT_TEXT = "n/a";
    private static String BINARY_ORIGINAL = "n/a";
    private static String BINARY_COMPRESSED = "n/a";
    private static String DECOMPRESSED_TEXT = "n/a";

    @RequestMapping(value = "*", method = RequestMethod.GET)
    public String index(Model model)
    {
        double compression_rate = BINARY_ORIGINAL.length() < 1
                ? 0
                : 1.0 * BINARY_COMPRESSED.length() / BINARY_ORIGINAL.length();

        model.addAttribute("text", INPUT_TEXT);
        model.addAttribute("binary_original", StringIO.groupByEights(BINARY_ORIGINAL));
        model.addAttribute("binary_compressed", StringIO.groupByEights(BINARY_COMPRESSED));
        model.addAttribute("compression_rate", compression_rate);
        model.addAttribute("decompressed", DECOMPRESSED_TEXT);

        return "index";
    }

    @RequestMapping(value = "/compress", method = RequestMethod.POST)
    public String compress(@RequestParam String input)
    {
        INPUT_TEXT = input;

        try
        {
            byte[] original = input.getBytes(StandardCharsets.UTF_8);
            byte[] bytes = Huffman.compress(original);
            BINARY_ORIGINAL = StringIO.toBinaryString(original);
            BINARY_COMPRESSED = StringIO.toBinaryString(bytes);
        }
        catch (Exception e)
        {
            INPUT_TEXT = "Something went wrong, fool. Check them logs.";
            BINARY_ORIGINAL = "n/a";
            BINARY_COMPRESSED = "n/a";
            e.printStackTrace();
        }

        return "redirect:/";
    }

    @RequestMapping(value = "/decompress", method = RequestMethod.POST)
    public String decompress(@RequestParam String input)
    {
        try
        {
            // below some ABSOLUTE BULLSHIT, only a temporary measure !!!
            byte[] bytes = new BigInteger(input.replaceAll("\\s+", ""), 2).toByteArray();
            DECOMPRESSED_TEXT = new String(Huffman.decompress(bytes), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            DECOMPRESSED_TEXT = "Something went wrong, fool. That ain't Huffman compressed binary, fool.";
            e.printStackTrace();
        }

        return "redirect:/";
    }
}
