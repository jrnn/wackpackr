package wackpackr.core;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import wackpackr.io.BinaryIO;

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
                    write16Bits(p, io);
                    D.put(pc, i++);
                    p = c + 128;
                }
            }
            write16Bits(p, io);
            write16Bits(MAX_DICTIONARY_SIZE - 1, io);  // eof

            return io.getBytesOut();
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO(bytes))
        {
            Map<Integer, ByteString> D = new TreeMap<>();
            int i = 0;
            for (; i < 256; i++)
                D.put(i, new ByteString((byte)(i - 128)));

            int c, p = read16Bits(io);
            ByteString out = D.get(p).copy();
            ByteString x, y;

            while (true)
            {
                c = read16Bits(io);
                x = D.get(p).copy();

                if (c == MAX_DICTIONARY_SIZE - 1)  // eof
                    break;

                if (D.containsKey(c))
                {
                    y = D.get(c);
                    D.put(i++, x.append(y.get(0)));
                    out.append(y);
                }
                else
                {
                    x.append(D.get(p).get(0));
                    D.put(i++, x);
                    out.append(x);
                }

                p = c;
            }

            return out.getBytes();
        }
    }

    private static int read16Bits(BinaryIO io) throws IOException
    {
        int i = 0;

        for (int k = 0; k < 2; k++)
        {
            i <<= 8;
            i |= (io.readByte() & 0xFF);
        }

        return i;
    }

    private static void write16Bits(int i, BinaryIO io) throws IOException
    {
        io
                .writeByte((byte) (i >> 8))
                .writeByte((byte) (i & 0xFF));
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

    private static final class ByteString
    {
        byte[] bytes = new byte[8];
        int i = 0;

        ByteString(byte... bs)
        {
            append(bs);
        }

        private void add(byte b)
        {
            if (i == bytes.length)
                expand();
            bytes[i++] = b;
        }

        ByteString append(byte... bs)
        {
            for (byte b : bs)
                add(b);
            return this;
        }

        ByteString append(ByteString bs)
        {
            append(bs.getBytes());
            return this;
        }

        byte get(int index)
        {
            return bytes[index];
        }

        byte[] getBytes()
        {
            byte[] res = new byte[i];
            System.arraycopy(bytes, 0, res, 0, i);
            return res;
        }

        ByteString copy()
        {
            ByteString copy = new ByteString(bytes);
            copy.i = i;
            return copy;
        }

        private void expand()
        {
            byte[] newBytes = new byte[bytes.length << 1];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            bytes = newBytes;
        }
    }
}
