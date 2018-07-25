package wackpackr.io;

import java.nio.charset.StandardCharsets;

/**
 *  This just some temporary bullshit, won't bother to comment.
 * 
 *  @author jjuurine
 */
public class StringIO
{
    public static String toBinaryString(String s)
    {
        StringBuilder sb = new StringBuilder();

        // hack to deal with "signed" property of Java bytes
        for (byte b : s.getBytes(StandardCharsets.UTF_8))
            sb.append(Integer.toBinaryString((b & 0xFF) + 0x100).substring(1));

        return sb.toString();
    }

    public static String groupByEights(String s)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++)
        {
            if (i % 8 == 0)
                sb.append(" ");
            sb.append(s.charAt(i));
        }

        return sb.toString();
    }
}
