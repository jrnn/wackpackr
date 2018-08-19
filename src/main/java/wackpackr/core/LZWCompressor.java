package wackpackr.core;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import wackpackr.config.Constants;
import wackpackr.io.BinaryIO;
import wackpackr.util.ByteString;

/**
 * All work and no play makes Jack a dull boy. Horribly messy and WIP application of LZW, look away.
 * Nothing to see here, hombre. NOTHING TO SEE HERE.
 *
 * @author Juho Juurinen
 */
public class LZWCompressor implements Compressor
{
    private static final int CODEWORD_BITSIZE = 16;
    private static final int MAX_DICTIONARY_SIZE = 1 << CODEWORD_BITSIZE;

    @Override
    public byte[] compress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO())
        {
            io.write32Bits(Constants.LZW_TAG);

            Map<Codeword, Integer> D = new TreeMap<>();
            int i = 0;
            for (; i < 256; i++)
                D.put(new Codeword(-1, (byte)(i - 128)), i);

            int p = -1;
            Codeword pc;

            for (byte c : bytes)
            {
                pc = new Codeword(p, c);

                if (D.containsKey(pc))
                    p = D.get(pc);
                else
                {
                    io.write16Bits(p);
                    D.put(pc, i++);
                    p = c + 128;
                }
            }
            io.write16Bits(p);
            io.write16Bits(MAX_DICTIONARY_SIZE - 1);    // eof

            return io.getBytesOut();
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO(bytes))
        {
            if (io.read32Bits() != Constants.LZW_TAG)
                throw new IllegalArgumentException("Not a LZW compressed file");

            Map<Integer, ByteString> D = new TreeMap<>();
            int i = 0;
            for (; i < 256; i++)
                D.put(i, new ByteString((byte)(i - 128)));

            int c, p = io.read16Bits();
            ByteString out = D.get(p).copy();
            ByteString x, y;

            while (true)
            {
                c = io.read16Bits();
                x = D.get(p).copy();

                if (c == MAX_DICTIONARY_SIZE - 1)  // eof
                    break;

                if (D.containsKey(c))
                {
                    y = D.get(c);
                    D.put(i++, x.append(y.byteAt(0)));
                    out.append(y);
                }
                else
                {
                    D.put(i++, x.append(D.get(p).byteAt(0)));
                    out.append(x);
                }

                p = c;
            }

            return out.getBytes();
        }
    }

    @Override
    public String getName() {
        return "LZW";
    }

    private static final class Codeword implements Comparable<Codeword>
    {
        int index;
        byte value;

        Codeword(int index, byte value)
        {
            this.index = index;
            this.value = value;
        }

        @Override
        public int compareTo(Codeword o)
        {
            int i = Integer.compare(index, o.index);
            return (i != 0)
                    ? i
                    : Byte.compare(value, o.value);
        }
    }
}
