package wackpackr.web;

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
        BINARY_ORIGINAL = StringIO.toBinaryString(input);
        BINARY_COMPRESSED = Huffman.compress(input);

        return "redirect:/";
    }

    @RequestMapping(value = "/decompress", method = RequestMethod.POST)
    public String decompress(@RequestParam String input)
    {
        try
        {
            DECOMPRESSED_TEXT = Huffman.decompress(input.replaceAll("\\s+", ""));
        }
        catch (Exception e)
        {
            DECOMPRESSED_TEXT = "Something went wrong, fool. That ain't Huffman compressed binary, fool.";
            e.printStackTrace();
        }

        return "redirect:/";
    }
}
