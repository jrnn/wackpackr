package wackpackr.core;

import java.io.IOException;
import wackpackr.config.Constants;
import wackpackr.io.BinaryIO;
import wackpackr.util.ByteString;

/**
 * LZW that's starting to perform nicely. JavaDoc and refactoring still needed.
 *
 * @author Juho Juurinen
 */
public class LZWCompressor implements Compressor
{
    @Override
    public byte[] compress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO())
        {
            int bitsize = 9;
            io.write32Bits(Constants.LZW_TAG);

            LZWDictionary dict = new LZWDictionary();
            int index = -1, newIndex;

            for (byte b : bytes)
            {
                newIndex = dict.get(index, b);

                if (newIndex > 0)
                    index = newIndex;
                else
                {
                    write(io, bitsize, index);
                    bitsize = dict.put(index, b);
                    index = b + 129;  // this should be = dict.get(-1, b) but we can cut corners
                }
                dict.resetIfFull();
            }
            write(io, bitsize, index);
            io.write32Bits(0);      // EoF marker + padding

            return io.getBytesOut();
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) throws IOException
    {
        try (BinaryIO io = new BinaryIO(bytes))
        {
            int bitsize = 9;
            if (io.read32Bits() != Constants.LZW_TAG)
                throw new IllegalArgumentException("Not a LZW compressed file");

            LZWDictionary dict = new LZWDictionary();
            ByteString x, y;

            int index = read(io, bitsize);
            int newIndex = read(io, bitsize);
            io.writeBytes(dict.get(index).getBytes());

            while (newIndex != 0)
            {
                x = dict.get(index).copy();
                y = dict.get(newIndex);

                if (y != null)
                {
                    bitsize = dict.put(x.append(y.byteAt(0)));
                    io.writeBytes(y.getBytes());
                }
                else
                {
                    bitsize = dict.put(x.append(x.byteAt(0)));
                    io.writeBytes(x.getBytes());
                }

                index = newIndex;
                newIndex = read(io, bitsize);
            }

            return io.getBytesOut();
        }
    }

    @Override
    public String getName() {
        return "LZW";
    }

    private void write(BinaryIO io, int bitSize, int value) throws IOException
    {
        while (bitSize > 0)
        {
            bitSize--;
            io.writeBit(((value >> bitSize) & 1) == 1);
        }
    }

    private int read(BinaryIO io, int bitSize) throws IOException
    {
        int i = 0;

        for (; bitSize > 0; bitSize--)
        {
            i <<= 1;
            i |= (io.readBit() ? 1 : 0);
        }

        return i;
    }
}
