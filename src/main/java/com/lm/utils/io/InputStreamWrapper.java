package com.lm.utils.io;

import com.lm.utils.io.bean.IOBuffer;
import com.lm.utils.io.bean.IOBufferPool;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream包装类
 *
 * @author LM
 * @version 2019-05-16 v1.0.0
 */
public class InputStreamWrapper {

    private InputStream inputStream;
    private IOBufferPool pool;
    private IOBuffer head;
    private long size;

    public InputStreamWrapper(InputStream inputStream) {
        this.inputStream = inputStream;
        this.pool = new IOBufferPool();
    }

    /**
     * 读一个字节 (大端)
     *
     * @return 一个字节
     * @throws IOException IO异常
     */
    public byte readByte() throws IOException {
        require(1);

        IOBuffer buffer = head;

        byte result = buffer.data[buffer.pos++];
        size -= 1;

        recycleBufferIfNecessary(buffer);

        return result;
    }

    /**
     * 读两个字节 (小端)
     *
     * @return 两个字节
     * @throws IOException IO异常
     */
    public short readShortLE() throws IOException {
        return ConvertUtils.reverseBytesShort(readShort());
    }

    /**
     * 读两个字节 (大端)
     *
     * @return 两个字节
     * @throws IOException IO异常
     */
    public short readShort() throws IOException {
        require(2);

        IOBuffer buffer = head;

        if (buffer.limit - buffer.pos < 2) {
            return (short) (((readByte() & 0xff) << 8) | ((readByte() & 0xff)));
        }

        short result = (short) (((buffer.data[buffer.pos++] & 0xff) << 8) | (buffer.data[buffer.pos++] & 0xff));
        size -= 2;

        recycleBufferIfNecessary(buffer);

        return result;
    }

    /**
     * 读四个字节 (小端)
     *
     * @return 四个字节
     * @throws IOException IO异常
     */
    public int readIntLE() throws IOException {
        return ConvertUtils.reverseBytesInt(readInt());
    }

    /**
     * 读四个字节 (大端)
     *
     * @return 四个字节
     * @throws IOException IO异常
     */
    public int readInt() throws IOException {
        require(4);

        IOBuffer buffer = head;

        if (buffer.limit - buffer.pos < 4) {
            return ((readByte() & 0xff) << 24)
                    | ((readByte() & 0xff) << 16)
                    | ((readByte() & 0xff) << 8)
                    | (readByte() & 0xff);
        }

        int result = ((buffer.data[buffer.pos++] & 0xff) << 24)
                | ((buffer.data[buffer.pos++] & 0xff) << 16)
                | ((buffer.data[buffer.pos++] & 0xff) << 8)
                | (buffer.data[buffer.pos++] & 0xff);
        size -= 4;

        recycleBufferIfNecessary(buffer);

        return result;
    }

    /**
     * 读八个字节 (小端)
     *
     * @return 八个字节
     * @throws IOException IO异常
     */
    public long readLongLE() throws IOException {
        return ConvertUtils.reverseBytesLong(readLong());
    }

    /**
     * 读八个字节 (大端)
     *
     * @return 八个字节
     * @throws IOException IO异常
     */
    public long readLong() throws IOException {
        require(8);

        IOBuffer buffer = head;

        if (buffer.limit - buffer.pos < 8) {
            return ((readInt() & 0xffffffffL) << 32) | (readInt() & 0xffffffffL);
        }

        long result = ((buffer.data[buffer.pos++] & 0xffL) << 56)
                | ((buffer.data[buffer.pos++] & 0xffL) << 48)
                | ((buffer.data[buffer.pos++] & 0xffL) << 40)
                | ((buffer.data[buffer.pos++] & 0xffL) << 32)
                | ((buffer.data[buffer.pos++] & 0xffL) << 24)
                | ((buffer.data[buffer.pos++] & 0xffL) << 16)
                | ((buffer.data[buffer.pos++] & 0xffL) << 8)
                | (buffer.data[buffer.pos++] & 0xffL);

        size -= 8;

        recycleBufferIfNecessary(buffer);

        return result;
    }

    /**
     * 读一堆字节
     *
     * @param readCount 要读个数
     * @return 读取的一堆字节
     * @throws IOException IO异常
     */
    public byte[] readBytes(long readCount) throws IOException {
        if (readCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("readCount > Integer.MAX_VALUE: " + readCount);
        } else if (readCount <= 0) {
            throw new IllegalArgumentException("readCount <= 0: " + readCount);
        }

        require(readCount);

        int offset = 0;
        byte[] result = new byte[(int) readCount];
        while (offset < readCount) {
            int maxCanReadCount = (int) Math.min(readCount - offset, head.limit - head.pos);
            System.arraycopy(head.data, head.pos, result, offset, maxCanReadCount);
            offset += maxCanReadCount;
            head.pos += maxCanReadCount;
            size -= maxCanReadCount;
            recycleBufferIfNecessary(head);
        }
        return result;
    }

    /**
     * 读一行字符串（UTF8编码）
     *
     * @return 一行字符串（UTF8编码）
     * @throws IOException IO异常
     */
    public String readUTF8Line() throws IOException {
        return readLine("UTF-8");
    }

    /**
     * 读一行字符串
     *
     * @param charsetName 字符编码
     * @return 一行字符串
     * @throws IOException IO异常
     */
    public String readLine(String charsetName) throws IOException {
        long newLine = indexOf((byte) '\n');
        if (newLine == -1) {
            return size != 0 ? readString(size, charsetName) : null;
        }

        if (newLine > 0 && valueOf(newLine - 1) == '\r') {
            String result = readString(newLine - 1, charsetName);
            skip(2);
            return result;
        } else {
            String result = readString(newLine, charsetName);
            skip(1);
            return result;
        }
    }

    /**
     * 读字符串
     *
     * @param readCount   读的个数
     * @param charsetName 字符串编码
     * @return 字符串
     * @throws IOException IO异常
     */
    public String readString(long readCount, String charsetName) throws IOException {
        if (readCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("readCount > Integer.MAX_VALUE: " + readCount);
        }

        require(readCount);

        if (readCount == 0) return "";

        IOBuffer buffer = head;

        if (buffer.limit - buffer.pos < readCount) {
            return new String(readBytes(readCount), charsetName);
        }

        String result = new String(buffer.data, buffer.pos, (int) readCount, charsetName);
        buffer.pos += readCount;
        size -= readCount;
        recycleBufferIfNecessary(buffer);
        return result;
    }

    /**
     * 读取所有数据（UTF8编码）
     *
     * @return 所有数据（UTF8编码）
     * @throws IOException IO异常
     */
    public String readAllUTF8() throws IOException {
        return readAll("UTF-8");
    }

    /**
     * 读取所有数据
     *
     * @param charsetName 字符串编码
     * @return 所有数据
     * @throws IOException IO异常
     */
    public String readAll(String charsetName) throws IOException {
        requireAll();
        return readString(size, charsetName);
    }

    /**
     * 跳过字节
     *
     * @param skipCount 跳过字节个数
     * @throws IOException IO异常
     */
    public void skip(long skipCount) throws IOException {
        require(skipCount);
        while (skipCount > 0) {
            int canSkipMaxCount = (int) Math.min(skipCount, head.limit - head.pos);
            head.pos += canSkipMaxCount;
            skipCount -= canSkipMaxCount;
            size -= canSkipMaxCount;
            recycleBufferIfNecessary(head);
        }
    }

    /**
     * 获取index位置的字节内容
     *
     * @param index 位置
     * @return 内容
     */
    public byte valueOf(long index) {
        if (index > size)
            throw new ArrayIndexOutOfBoundsException("index > size: index=" + index + " size=" + size);

        IOBuffer buffer = head;
        while (true) {
            int canReadCount = buffer.limit - buffer.pos;
            if (index < canReadCount) return buffer.data[buffer.pos + (int) index];
            index -= canReadCount;
            buffer = buffer.next;
        }
    }

    /**
     * 查找字符位置（如果缓冲区中查找不到，就继续将输入流读入缓冲区，直到查找到或者无法继续读入缓冲区）
     *
     * @param content 要查找的字符
     * @return 查找字符的位置
     * @throws IOException IO异常
     */
    public long indexOf(byte content) throws IOException {
        return indexOf(content, 0);
    }

    /**
     * 查找字符位置（如果缓冲区中查找不到，就继续将输入流读入缓冲区，直到查找到或者无法继续读入缓冲区）
     *
     * @param content    要查找的字符
     * @param startIndex 开始坐标
     * @return 查找字符的位置
     * @throws IOException IO异常
     */
    public long indexOf(byte content, long startIndex) throws IOException {
        long resultIndex;
        while ((resultIndex = findInBuffer(content, startIndex)) == -1) {
            startIndex = size;
            if (read(IOBuffer.BUFFER_SIZE) == -1) return -1;
        }
        return resultIndex;
    }

    /**
     * 查找字符在缓冲区的位置
     *
     * @param content    要查找的字符
     * @param startIndex 开始坐标
     * @return 在缓冲区的位置
     */
    private long findInBuffer(byte content, long startIndex) {

        if (head == null) return -1L;

        IOBuffer buffer = head;
        long offset = 0L;

        do {
            int bufferDataCount = buffer.limit - buffer.pos;

            if (startIndex >= bufferDataCount) {
                startIndex -= bufferDataCount;
            } else {
                for (long pos = buffer.pos + startIndex, limit = buffer.limit; pos < limit; pos++) {
                    if (buffer.data[(int) pos] == content) {
                        return offset + pos - buffer.pos;
                    }
                }
                startIndex = 0;
            }
            offset += bufferDataCount;
            buffer = buffer.next;
        } while (buffer != head);

        return -1L;
    }

    /**
     * 请求读入缓冲区
     *
     * @param requireCount 请求读入个数
     * @throws IOException IO异常
     */
    private void require(long requireCount) throws IOException {
        while (size < requireCount) {
            if (read(IOBuffer.BUFFER_SIZE) == -1) throw new EOFException();
        }
    }

    /**
     * 请求所以数据读入缓冲区
     *
     * @throws IOException IO异常
     */
    private void requireAll() throws IOException {
        while (true) {
            if (read(IOBuffer.BUFFER_SIZE) == -1) break;
        }
    }

    /**
     * 读入缓冲区
     *
     * @param requireCount 请求读入个数
     * @return 实际读入缓冲区数据个数
     * @throws IOException IO异常
     */
    private long read(long requireCount) throws IOException {
        if (requireCount < 0)
            throw new IllegalArgumentException("requireCount < 0: " + requireCount);
        IOBuffer buffer = getBuffer(1);
        int maxCanReadCount = (int) Math.min(requireCount, IOBuffer.BUFFER_SIZE - buffer.limit);
        int readCount = inputStream.read(buffer.data, buffer.limit, maxCanReadCount);
        if (readCount == -1) return -1;
        buffer.limit += readCount;
        size += readCount;
        return readCount;
    }

    /**
     * 获取请求读入的缓冲区
     *
     * @param minimumCapacity 最小容量
     * @return 缓冲区
     */
    private IOBuffer getBuffer(int minimumCapacity) {
        if (minimumCapacity < 1 || minimumCapacity > IOBuffer.BUFFER_SIZE)
            throw new IllegalArgumentException();

        if (head == null) {
            head = pool.take();
            return head.next = head.previous = head;
        }

        IOBuffer buffer = head.previous;
        if (buffer.limit + minimumCapacity > IOBuffer.BUFFER_SIZE) {
            buffer = buffer.push(pool.take());
        }
        return buffer;
    }

    /**
     * 必要时回收缓冲区
     *
     * @param buffer 缓冲区
     */
    private void recycleBufferIfNecessary(IOBuffer buffer) {
        if (buffer.pos == buffer.limit) {
            head = buffer.pop();
            pool.recycle(buffer);
        }
    }

    /**
     * 关闭inputStream
     */
    public void close() {
        IOUtils.closeIO(inputStream);
    }
}
