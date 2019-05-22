package com.lm.utils.io.bean;

/**
 * IO缓冲区
 *
 * @author LM
 * @version 2019-05-16 v1.0.0
 */
public class IOBuffer {
    // 缓冲区大小
    public static final int BUFFER_SIZE = 2048;

    // 缓冲数据
    public byte[] data = new byte[BUFFER_SIZE];

    // 下一个要被读取的数据位置
    public int pos;

    // 下一个要被写入的数据位置
    public int limit;

    // 前一个缓冲区
    public IOBuffer previous;

    // 后一个缓冲区
    public IOBuffer next;

    // pop
    public IOBuffer pop() {
        IOBuffer result = next != this ? next : null;
        previous.next = next;
        next.previous = previous;
        next = null;
        previous = null;
        return result;
    }

    // push
    public IOBuffer push(IOBuffer buffer) {
        buffer.previous = this;
        buffer.next = next;
        next.previous = buffer;
        next = buffer;
        return buffer;
    }
}
