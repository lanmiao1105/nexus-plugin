package com.lm.utils.io.bean;

/**
 * IO缓冲池
 *
 * @author LM
 * @version 2019-05-16 v1.0.0
 */
public class IOBufferPool {

    public static final long MAX_SIZE = 64 * 1024;

    private IOBuffer head;
    private long byteCount;

    public IOBuffer take() {
        IOBuffer buffer;
        if (head != null) {
            synchronized (this) {
                buffer = head;
                head = buffer.next;
                buffer.next = null;
                byteCount -= IOBuffer.BUFFER_SIZE;
            }
        } else {
            buffer = new IOBuffer();
        }
        return buffer;
    }

    public void recycle(IOBuffer buffer) {
        if (buffer.next != null || buffer.previous != null) throw new IllegalArgumentException();
        synchronized (this) {
            if (byteCount + IOBuffer.BUFFER_SIZE > MAX_SIZE) return;
            byteCount += IOBuffer.BUFFER_SIZE;
            buffer.next = head;
            buffer.pos = buffer.limit = 0;
            head = buffer;
        }
    }
}
