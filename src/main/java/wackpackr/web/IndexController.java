package wackpackr.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class IndexController
{
    @Autowired
    private CompressionService compressionService;

    @RequestMapping(value = "*", method = RequestMethod.GET)
    public String index(Model model)
    {
        model.addAttribute("results", compressionService.getResults());

        return "index";
    }

    @RequestMapping(value = "/compress", method = RequestMethod.POST)
    public String compress(@RequestParam("file") MultipartFile file)
    {
        if (file != null && file.getSize() > 0)
            try
            {
                compressionService.runTests(file.getBytes());
            }
            catch (Exception e) {}

        return "redirect:/";
    }
}
