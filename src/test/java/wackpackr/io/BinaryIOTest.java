package wackpackr.io;

import java.io.EOFException;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BinaryIOTest
{
    private String binary;
    private byte[] bytes;

    @Before
    public void before()
    {
        bytes = new byte[1024];
        ThreadLocalRandom.current().nextBytes(bytes);
        binary = byteArrayToBinaryString(bytes);
    }

    @Test
    public void readsBitsCorrectly() throws Exception
    {
        try (BinaryIO io = new BinaryIO(bytes))
        {
            for (int i = 0; i < bytes.length; i++)
            {
                binary = "";

                for (int j = 0; j < 8; j++)
                    binary += io.readBit()
                            ? "1"
                            : "0";

                Assert.assertEquals(
                        bytes[i],
                        (byte) Integer.parseInt(binary, 2)
                );
            }
        }
    }

    @Test
    public void readsBytesCorrectlyAtAllOffsets() throws Exception
    {
        for (int offset = 0; offset <= 8; offset++)
            try (BinaryIO io = new BinaryIO(bytes))
            {
                for (int i = 0; i < offset; i++)
                    io.readBit();

                for (int i = 0; i < binary.length() / 8; i++)
                    Assert.assertEquals(
                            io.readByte(),
                            (byte) Integer.parseInt(binary.substring(i * 8, i * 8 + 8), 2)
                    );

                binary = binary.substring(1);
            }
    }

    @Test
    public void readsSeveralBytesCorrectlyAtAllOffsets() throws Exception
    {
        for (int offset = 0; offset <= 8; offset++)
            try (BinaryIO io = new BinaryIO(bytes))
            {
                int n = ThreadLocalRandom.current().nextInt(2, 256);

                for (int i = 0; i < offset; i++)
                    io.readBit();

                for (int i = 0; i < (binary.length() / 8) - (n * 8); i += n)
                {
                    byte[] chunk = io.readBytes(n);
                    for (int k = 0; k < n; k++)
                        Assert.assertEquals(
                                chunk[k],
                                (byte) Integer.parseInt(binary.substring((i + k) * 8, (i + k) * 8 + 8), 2)
                        );
                }

                binary = binary.substring(1);
            }
    }

    @Test
    public void reads32BitChunksCorrectly() throws Exception
    {
        for (int offset = 0; offset <= 32; offset++)
        {
            try (BinaryIO io = new BinaryIO(bytes))
            {
                for (int i = 0; i < offset; i++)
                    io.readBit();

                for (int i = 0; i < binary.length() / 32; i++)
                    Assert.assertEquals(
                            io.read32Bits(),
                            Long.parseLong(binary.substring(i * 32, i * 32 + 32), 2)
                    );

                binary = binary.substring(1);
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void readBitWithoutInputStreamThrowsException() throws Exception
    {
        BinaryIO io = new BinaryIO();
        io.readBit();
    }

    @Test(expected = NullPointerException.class)
    public void readByteWithoutInputStreamThrowsException() throws Exception
    {
        BinaryIO io = new BinaryIO();
        io.readByte();
    }

    @Test(expected = EOFException.class)
    public void readBitWhenInputStreamHasEndedThrowsException() throws Exception
    {
        BinaryIO io = new BinaryIO(new byte[]{-1});
        for (int i = 0; i < 9; i++)
            io.readBit();
    }

    @Test(expected = EOFException.class)
    public void readByteWhenInputStreamHasLessThan8BitsThrowsException() throws Exception
    {
        BinaryIO io = new BinaryIO(new byte[]{-1, 0, 1});
        io.readBit();
        for (int i = 0; i < 9; i++)
            io.readByte();
    }

    @Test
    public void writesBitsCorrectly() throws Exception
    {
        MockBitStream bs = new MockBitStream(binary);

        try (BinaryIO io = new BinaryIO())
        {
            while (bs.hasNext())
                io.writeBit(bs.nextBit());

            Assert.assertArrayEquals(
                    bytes,
                    io.getBytesOut()
            );
        }
    }

    @Test
    public void writesBytesCorrectlyAtAllOffsets() throws Exception
    {
        for (int offset = 0; offset <= 8; offset++)
            try (BinaryIO io = new BinaryIO())
            {
                MockBitStream bs = new MockBitStream(binary);

                for (int i = 0; i < offset; i++)
                    io.writeBit(bs.nextBit());

                while (bs.length() >= 8)
                    io.writeByte(bs.nextByte());

                while (bs.hasNext())
                    io.writeBit(bs.nextBit());

                Assert.assertArrayEquals(
                        bytes,
                        io.getBytesOut()
                );
            }
    }

    @Test
    public void writesSeveralBytesCorrectlyAtAllOffsets() throws Exception
    {
        for (int offset = 0; offset <= 8; offset++)
            try (BinaryIO io = new BinaryIO())
            {
                MockBitStream bs = new MockBitStream(binary);
                int n = ThreadLocalRandom.current().nextInt(2, 256);
                byte[] chunk = new byte[n];

                for (int i = 0; i < offset; i++)
                    io.writeBit(bs.nextBit());

                while (bs.length() >= (8 * n))
                {
                    for (int k = 0; k < n; k++)
                        chunk[k] = bs.nextByte();

                    io.writeBytes(chunk);
                }

                while (bs.hasNext())
                    io.writeBit(bs.nextBit());

                Assert.assertArrayEquals(
                        bytes,
                        io.getBytesOut()
                );
            }
    }

    @Test
    public void writes32BitChunksCorrectly() throws Exception
    {
        for (int offset = 0; offset <= 32; offset++)
            try (BinaryIO io = new BinaryIO())
            {
                MockBitStream bs = new MockBitStream(binary);

                for (int i = 0; i < offset; i++)
                    io.writeBit(bs.nextBit());

                while (bs.length() >= 32)
                    io.write32Bits(bs.next32Bits());

                while (bs.hasNext())
                    io.writeBit(bs.nextBit());

                Assert.assertArrayEquals(
                        bytes,
                        io.getBytesOut()
                );
            }
    }

    @Test
    public void writingMethodsCanBeChained() throws Exception
    {
        try (BinaryIO io = new BinaryIO())
        {
            io
                    .writeBit(true)
                    .writeByte((byte) 42)
                    .writeBit(false)
                    .writeBytes(new byte[]{ -13, 42, 77 })
                    .writeBit(true)
                    .write32Bits(123456789)
                    .writeBit(false);

            Assert.assertArrayEquals(
                    io.getBytesOut(),
                    new byte[]{ -107, 60, -54, -109, 96, -21, 121, -94 }
            );
        }
    }

    private String byteArrayToBinaryString(byte[] bs)
    {
        StringBuilder sb = new StringBuilder();

        for (int b : bs)
        {
            String s = Integer.toBinaryString(b + 256);
            sb.append(s.substring(s.length() - 8, s.length()));
        }

        return sb.toString();
    }
}
