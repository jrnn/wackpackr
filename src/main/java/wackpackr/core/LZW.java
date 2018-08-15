package wackpackr.core;

import java.util.HashMap;
import java.util.Map;

/**
 * All work and no play makes Jack a dull boy
 *
 * @author Juho Juurinen
 */
public class LZW
{
    public static void compress(String s)
    {
        // initiate dictionary (note, should have all "single-byte values" in order, but doing first
        // a limited shit version, just for learning purposes ..........)
        Map<String, Integer> D = new HashMap<>();
        int i = 0;
        for (char c : s.toCharArray())
            if (!D.containsKey("" + c))
                D.put("" + c, i++);

//        for (int b = -128; b < 128; b++)
//            D.put("" + (char) b, i++);

        String p = "";
        String out = "";

        for (char c : s.toCharArray())
        {
            if (D.containsKey(p + c))
                p = p + c;
            else
            {
                out = out + D.get(p) + ":";
                D.put(p + c, i++);
                p = "" + c;
            }
        }

        Map<Integer, String> D2 = new HashMap<>();
        D.entrySet().forEach(e -> D2.put(e.getValue(), e.getKey()));

        System.out.println(D2);
        System.out.println(out);

        for (String x : out.split(":"))
            System.out.print(D2.get(Integer.parseInt(x)));
        System.out.println();
    }
}
