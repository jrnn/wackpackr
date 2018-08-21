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
                    io.writeBits(index, bitsize);
                    bitsize = dict.put(index, b);
                    index = b + 129;  // this should be = dict.get(-1, b) but we can cut corners
                }
                dict.resetIfFull();
            }
            io
                    .writeBits(index, bitsize)
                    .write32Bits(0);      // EoF marker + padding

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

            int index = io.readBits(bitsize);
            int newIndex = io.readBits(bitsize);
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
                newIndex = io.readBits(bitsize);
            }

            return io.getBytesOut();
        }
    }

    @Override
    public String getName() {
        return "LZW";
    }
}
