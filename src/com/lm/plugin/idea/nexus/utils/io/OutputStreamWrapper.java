package com.lm.plugin.idea.nexus.utils.io;

import com.lm.plugin.idea.nexus.utils.io.bean.IOBuffer;
import com.lm.plugin.idea.nexus.utils.io.bean.IOBufferPool;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream包装类
 *
 * @author LM
 * @version 2019-05-16 v1.0.0
 */
public class OutputStreamWrapper {

    private OutputStream outputStream;
    private IOBufferPool pool;
    private IOBuffer head;
    private long size;

    public OutputStreamWrapper(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.pool = new IOBufferPool();
    }

    /**
     * 写入1个字节
     *
     * @param content byte
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper writeByte(byte content) throws IOException {
        IOBuffer buffer = getBuffer(1);
        buffer.data[buffer.limit++] = content;
        size += 1;
        commit();
        return this;
    }

    /**
     * 写入2个字节（小端）
     *
     * @param content short
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper writeShortLE(short content) throws IOException {
        return writeShort(ConvertUtils.reverseBytesShort(content));
    }

    /**
     * 写入2个字节（大端）
     *
     * @param content short
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper writeShort(short content) throws IOException {
        IOBuffer buffer = getBuffer(2);
        buffer.data[buffer.limit++] = (byte) ((content >>> 8) & 0xff);
        buffer.data[buffer.limit++] = (byte) (content & 0xff);
        size += 2;
        commit();
        return this;
    }

    /**
     * 写入4个字节（小端）
     *
     * @param content int
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper writeIntLE(int content) throws IOException {
        return writeInt(ConvertUtils.reverseBytesInt(content));
    }

    /**
     * 写入4个字节（大端）
     *
     * @param content int
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper writeInt(int content) throws IOException {
        IOBuffer buffer = getBuffer(4);
        buffer.data[buffer.limit++] = (byte) ((content >>> 24) & 0xff);
        buffer.data[buffer.limit++] = (byte) ((content >>> 16) & 0xff);
        buffer.data[buffer.limit++] = (byte) ((content >>> 8) & 0xff);
        buffer.data[buffer.limit++] = (byte) (content & 0xff);
        size += 4;
        return this;
    }

    /**
     * 写入8个字节（小端）
     *
     * @param content long
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper writeLongLE(long content) throws IOException {
        return writeLong(ConvertUtils.reverseBytesLong(content));
    }

    /**
     * 写入8个字节（大端）
     *
     * @param content long
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper writeLong(long content) throws IOException {
        IOBuffer buffer = getBuffer(8);
        buffer.data[buffer.limit++] = (byte) ((content >>> 56L) & 0xff);
        buffer.data[buffer.limit++] = (byte) ((content >>> 48L) & 0xff);
        buffer.data[buffer.limit++] = (byte) ((content >>> 40L) & 0xff);
        buffer.data[buffer.limit++] = (byte) ((content >>> 32L) & 0xff);
        buffer.data[buffer.limit++] = (byte) ((content >>> 24L) & 0xff);
        buffer.data[buffer.limit++] = (byte) ((content >>> 16L) & 0xff);
        buffer.data[buffer.limit++] = (byte) ((content >>> 8L) & 0xff);
        buffer.data[buffer.limit++] = (byte) (content & 0xff);
        size += 8;
        return this;
    }

    /**
     * 写入UTF8字符串
     *
     * @param content UTF8字符串
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper writeUTF8(String content) throws IOException {
        return writeString(content, "UTF-8");
    }

    /**
     * 写入字符串
     *
     * @param content     字符串
     * @param charsetName 编码
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper writeString(String content, String charsetName) throws IOException {
        return writeBytes(content.getBytes(charsetName));
    }

    /**
     * 写入字节数组
     *
     * @param content 字节数组
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper writeBytes(byte[] content) throws IOException {
        return writeBytes(content, 0, content.length);
    }

    /**
     * 写入字节数据
     *
     * @param content 字节数据
     * @param off     起始下标
     * @param len     写入长度
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper writeBytes(byte[] content, int off, int len) throws IOException {
        int limit = off + len;
        while (off < limit) {
            IOBuffer buffer = getBuffer(1);
            int maxCanWriteCount = Math.min(limit - off, IOBuffer.BUFFER_SIZE - buffer.limit);
            System.arraycopy(content, off, buffer.data, buffer.pos, maxCanWriteCount);
            off += maxCanWriteCount;
            buffer.limit += maxCanWriteCount;
        }
        size += len;
        commit();
        return this;
    }


    /**
     * 将剩余内容全部提交写入
     *
     * @return OutputStreamWrapper
     * @throws IOException IO异常
     */
    public OutputStreamWrapper flush() throws IOException {
        if (size == 0) return this;

        realWrite(size);

        return this;
    }

    /**
     * 提交写入操作（保留一个缓冲区，其他全部写入）
     *
     * @throws IOException IO异常
     */
    private void commit() throws IOException {
        if (size == 0) return;

        long needWriteCount = size;

        if (head.previous.limit < IOBuffer.BUFFER_SIZE) {
            needWriteCount -= head.previous.limit - head.previous.pos;
        }

        realWrite(needWriteCount);
    }

    /**
     * 真正的使用输出流进行写操作
     *
     * @param needWriteCount 需要写入的数量
     * @throws IOException IO异常
     */
    private void realWrite(long needWriteCount) throws IOException {
        while (needWriteCount > 0) {
            int maxCanWriteCount = (int) Math.min(needWriteCount, head.limit - head.pos);
            outputStream.write(head.data, head.pos, maxCanWriteCount);

            head.pos += maxCanWriteCount;
            needWriteCount -= maxCanWriteCount;
            size -= maxCanWriteCount;

            recycleBufferIfNecessary(head);
        }
    }

    /**
     * 获取请求输出的缓冲区
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
     * 关闭outputStream
     */
    public void close() {
        IOUtils.closeIO(outputStream);
    }

}
