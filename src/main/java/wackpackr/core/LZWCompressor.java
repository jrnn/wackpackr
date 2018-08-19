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

            Map<Codeword, Integer> D = init();
            Codeword pc;
            int i = 256;
            int p = -1;

            for (byte c : bytes)
            {
                if (i == MAX_DICTIONARY_SIZE - 2)
                {
                    D = init();
                    i = 256;
                }
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
            io      // eof
                    .write16Bits(p)
                    .write16Bits(MAX_DICTIONARY_SIZE - 1)
                    .writeByte((byte) 0);

            return io.getBytesOut();
        }
    }

    private Map<Codeword, Integer> init()
    {
        Map<Codeword, Integer> D = new TreeMap<>();
        for (int i = 0; i < 256; i++)
            D.put(new Codeword(-1, (byte)(i - 128)), i);
        return D;
    }

    @Override
    public byte[] decompress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO(bytes))
        {
            if (io.read32Bits() != Constants.LZW_TAG)
                throw new IllegalArgumentException("Not a LZW compressed file");

            ByteString[] D = init2();
            int i = 256;

            ByteString x, y;
            int c, p = io.read16Bits();
            io.writeBytes(D[p].getBytes());

            while (true)
            {
                if (i == MAX_DICTIONARY_SIZE -2)
                {
                    D = init2();
                    i = 256;
                }
                c = io.read16Bits();
                x = D[p].copy();

                if (c == MAX_DICTIONARY_SIZE - 1)  // eof
                    break;

                if (D[c] != null)
                {
                    y = D[c];
                    D[i++] = x.append(y.byteAt(0));
                    io.writeBytes(y.getBytes());
                }
                else
                {
                    D[i++] = x.append(D[p].byteAt(0));
                    io.writeBytes(x.getBytes());
                }

                p = c;
            }

            return io.getBytesOut();
        }
    }

    private ByteString[] init2()
    {
        ByteString[] D = new ByteString[MAX_DICTIONARY_SIZE];
        for (int i = 0; i < 256; i++)
            D[i] = new ByteString((byte)(i - 128));
        return D;
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
