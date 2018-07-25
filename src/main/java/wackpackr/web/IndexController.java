package wackpackr.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import wackpackr.compressor.Huffman;
import wackpackr.io.StringIO;

@Controller
public class IndexController
{
    private static String INPUT_TEXT = "n/a";
    private static String BINARY_ORIGINAL = "n/a";
    private static String BINARY_COMPRESSED = "n/a";

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

        return "index";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String post(@RequestParam String text)
    {
        INPUT_TEXT = text;
        BINARY_ORIGINAL = StringIO.toBinaryString(text);
        BINARY_COMPRESSED = Huffman.compress(text);

        return "redirect:/";
    }
}
