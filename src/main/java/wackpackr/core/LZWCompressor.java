package wackpackr.core;

import java.io.IOException;
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

            LZWDictionary dict = new LZWDictionary();
            int index = -1, newIndex;

            for (byte b : bytes)
            {
                newIndex = dict.get(index, b);

                if (newIndex > 0)
                    index = newIndex;
                else
                {
                    io.write16Bits(index);
                    dict.put(index, b);
                    index = b + 128;  // this should be = dict.get(-1, b) but we can cut corners
                }
            }
            io      // eof
                    .write16Bits(index)
                    .write16Bits(MAX_DICTIONARY_SIZE - 1)
                    .writeByte((byte) 0);

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

            LZWDictionary dict = new LZWDictionary();
            ByteString x, y;

            int index = io.read16Bits();
            int newIndex = io.read16Bits();
            io.writeBytes(dict.get(index).getBytes());

            while (newIndex != MAX_DICTIONARY_SIZE - 1)
            {
                x = dict.get(index).copy();
                y = dict.get(newIndex);

                if (y != null)
                {
                    dict.put(x.append(y.byteAt(0)));
                    io.writeBytes(y.getBytes());
                }
                else
                {
                    dict.put(x.append(x.byteAt(0)));
                    io.writeBytes(x.getBytes());
                }

                index = newIndex;
                newIndex = io.read16Bits();
            }

            return io.getBytesOut();
        }
    }

    @Override
    public String getName() {
        return "LZW";
    }
}
