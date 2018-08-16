package wackpackr.core;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.HashMap;
import java.util.Map;

/**
 * All work and no play makes Jack a dull boy
 *
 * @author Juho Juurinen
 */
public class LZW
{
    private static final int CODEWORD_BITSIZE = 16;
    private static final int MAX_DICTIONARY_SIZE = 1 << CODEWORD_BITSIZE;

    public static String compress(String in)
    {
        // init dictionary with all single-byte values 0 to 255
        Map<String, Integer> D = new HashMap<>();
        byte[] x = new byte[1];
        int i = 0;
        for (; i < 256; i++)
        {
            x[0] = (byte) i;
            D.put(new String(x, UTF_8), i);
        }

        // run the algorithm
        String c, p = "";
        String out = "";
        for (byte b : in.getBytes(UTF_8))
        {
            x[0] = b;
            c = new String(x, UTF_8);
            if (D.containsKey(p + c))
                p += c;
            else
            {
                out = out + D.get(p) + ":";
                D.put(p + c, i++);
                p = c;
            }
        }

        return out;
    }

    public static String decompress(String in)
    {
        // init dictionary with all single-byte values 0 to 255
        Map<Integer, String> D = new HashMap<>();
        byte[] x = new byte[1];
        int i = 0;
        for (; i < 256; i++)
        {
            x[0] = (byte) i;
            D.put(i, new String(x, UTF_8));
        }

        // run the algorithm
        String[] ss = in.split(":");
        int c = Integer.parseInt(ss[0]);
        String p = D.get(c);
        String out = p;

        for (int k = 1; k < ss.length; k++)
        {
            c = Integer.parseInt(ss[k]);
            if (D.containsKey(c))
            {
                D.put(i++, p + D.get(c).charAt(0));
                p = D.get(c);
            }
            else
            {
                D.put(i++, p + p.charAt(0));
                p = D.get(c);
            }
            out += p;
        }

        return out;
    }
}
