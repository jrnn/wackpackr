package wackpackr.web;

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
    private static String HEX_ORIGINAL = "n/a";
    private static String HEX_COMPRESSED = "n/a";
    private static String DECOMPRESSED_TEXT = "n/a";

    @RequestMapping(value = "*", method = RequestMethod.GET)
    public String index(Model model)
    {
        double compression_rate = HEX_ORIGINAL.equals("n/a")
                ? 0
                : 1.0 * HEX_COMPRESSED.replaceAll("\\s+", "").length() / HEX_ORIGINAL.replaceAll("\\s+", "").length();

        model.addAttribute("text", INPUT_TEXT);
        model.addAttribute("hex_original", HEX_ORIGINAL);
        model.addAttribute("hex_compressed", HEX_COMPRESSED);
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
            byte[] compressed = Huffman.compress(original);
            HEX_ORIGINAL = StringIO.bytesToHexString(original);
            HEX_COMPRESSED = StringIO.bytesToHexString(compressed);
        }
        catch (Exception e)
        {
            INPUT_TEXT = "Something went wrong, fool. Check them logs.";
            HEX_ORIGINAL = "n/a";
            HEX_COMPRESSED = "n/a";
            e.printStackTrace();
        }

        return "redirect:/";
    }

    @RequestMapping(value = "/decompress", method = RequestMethod.POST)
    public String decompress(@RequestParam String input)
    {
        try
        {
            byte[] compressed = StringIO.hexStringToBytes(input);
            DECOMPRESSED_TEXT = new String(Huffman.decompress(compressed), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            DECOMPRESSED_TEXT = "Something went wrong, fool. That ain't Huffman compressed binary, fool.";
            e.printStackTrace();
        }

        return "redirect:/";
    }
}
