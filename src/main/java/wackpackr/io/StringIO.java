package wackpackr.io;

/**
 *  This just some temporary bullshit, won't bother to comment.
 *
 *  @author jjuurine
 */
public class StringIO
{
    public static String bytesToHexString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes)
            sb                // stupid hack to deal with "signed" property of Java bytes
                    .append(Integer.toHexString((int)(b + 384)).substring(1))
                    .append(" ");

        return sb
                .toString()
                .toUpperCase();
    }

    public static byte[] hexStringToBytes(String s)
    {
        s = s.replaceAll("\\s+", "");
        byte[] bytes = new byte[s.length() / 2];
        int b, i = 0;

        while (s.length() > 1)
        {
            b = Integer.parseInt(s.substring(0, 2), 16);
            bytes[i] = (byte) (b - 128);
            i++;
            s = s.substring(2);
        }

        return bytes;
    }

    public static String toBinaryString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes)
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
