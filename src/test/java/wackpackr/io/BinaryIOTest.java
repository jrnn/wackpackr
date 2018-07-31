package wackpackr.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
        try (BinaryIO io = new BinaryIO(new ByteArrayInputStream(bytes)))
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
        {
            try (BinaryIO io = new BinaryIO(new ByteArrayInputStream(bytes)))
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
    }

    @Test
    public void reads32BitChunksCorrectly() throws Exception
    {
        for (int offset = 0; offset <= 32; offset++)
        {
            try (BinaryIO io = new BinaryIO(new ByteArrayInputStream(bytes)))
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
        BinaryIO io = new BinaryIO(new ByteArrayOutputStream());
        io.readBit();
    }

    @Test(expected = NullPointerException.class)
    public void readByteWithoutInputStreamThrowsException() throws Exception
    {
        BinaryIO io = new BinaryIO(new ByteArrayOutputStream());
        io.readByte();
    }

    @Test(expected = EOFException.class)
    public void readBitWhenInputStreamHasEndedThrowsException() throws Exception
    {
        BinaryIO io = new BinaryIO(new ByteArrayInputStream(new byte[]{-1}));
        for (int i = 0; i < 9; i++)
            io.readBit();
    }

    @Test(expected = EOFException.class)
    public void readByteWhenInputStreamHasLessThan8BitsThrowsException() throws Exception
    {
        BinaryIO io = new BinaryIO(new ByteArrayInputStream(new byte[]{-1, 0, 1}));
        io.readBit();
        for (int i = 0; i < 9; i++)
            io.readByte();
    }

    @Test
    public void writesBitsCorrectly() throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MockBitStream bs = new MockBitStream(binary);

        try (BinaryIO io = new BinaryIO(out))
        {
            while (bs.hasNext())
                io.writeBit(bs.nextBit());

            Assert.assertArrayEquals(
                    bytes,
                    out.toByteArray()
            );
        }
    }

    @Test
    public void writesBytesCorrectlyAtAllOffsets() throws Exception
    {
        for (int offset = 0; offset <= 8; offset++)
        {
            MockBitStream bs = new MockBitStream(binary);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try (BinaryIO io = new BinaryIO(out))
            {
                for (int i = 0; i < offset; i++)
                    io.writeBit(bs.nextBit());

                while (bs.length() >= 8)
                    io.writeByte(bs.nextByte());

                while (bs.hasNext())
                    io.writeBit(bs.nextBit());
            }

            Assert.assertArrayEquals(
                    bytes,
                    out.toByteArray()
            );
        }
    }

    @Test
    public void writes32BitChunksCorrectly() throws Exception
    {
        for (int offset = 0; offset <= 32; offset++)
        {
            MockBitStream bs = new MockBitStream(binary);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try (BinaryIO io = new BinaryIO(out))
            {
                for (int i = 0; i < offset; i++)
                    io.writeBit(bs.nextBit());

                while (bs.length() >= 32)
                    io.write32Bits(bs.next32Bits());

                while (bs.hasNext())
                    io.writeBit(bs.nextBit());
            }

            Assert.assertArrayEquals(
                    bytes,
                    out.toByteArray()
            );
        }
    }

    @Test(expected = NullPointerException.class)
    public void writeBitWithoutOutputStreamThrowsException() throws Exception
    {
        BinaryIO io = new BinaryIO(new ByteArrayInputStream(bytes));
        for (int i = 0; i < 9; i++)
            io.writeBit(true);
    }

    @Test(expected = NullPointerException.class)
    public void writeByteWithoutOutputStreamThrowsException() throws Exception
    {
        BinaryIO io = new BinaryIO(new ByteArrayInputStream(bytes));
        io.writeByte((byte) -1);
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
