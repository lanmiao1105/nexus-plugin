package com.lm.utils.io;

/**
 * 类型转换工具类
 *
 * @author LM
 * @version 2019-05-16 v1.0.0
 */
public class ConvertUtils {

    /**
     * @param content short
     * @return short转为byte[] (小端)
     */
    public static byte[] short2BytesLE(short content) {
        byte[] result = new byte[2];
        result[1] = (byte) ((content >>> 8) & 0xff);
        result[0] = (byte) (content & 0xff);
        return result;
    }

    /**
     * @param content short
     * @return short转为byte[] (大端)
     */
    public static byte[] short2Bytes(short content) {
        byte[] result = new byte[2];
        result[0] = (byte) ((content >>> 8) & 0xff);
        result[1] = (byte) (content & 0xff);
        return result;
    }

    /**
     * @param content int
     * @return int转为byte[] (小端)
     */
    public static byte[] int2BytesLE(int content) {
        byte[] result = new byte[4];
        result[3] = (byte) ((content >>> 24) & 0xff);
        result[2] = (byte) ((content >>> 16) & 0xff);
        result[1] = (byte) ((content >>> 8) & 0xff);
        result[0] = (byte) (content & 0xff);
        return result;
    }

    /**
     * @param content int
     * @return int转为byte[] (大端)
     */
    public static byte[] int2Bytes(int content) {
        byte[] result = new byte[4];
        result[0] = (byte) ((content >>> 24) & 0xff);
        result[1] = (byte) ((content >>> 16) & 0xff);
        result[2] = (byte) ((content >>> 8) & 0xff);
        result[3] = (byte) (content & 0xff);
        return result;
    }

    /**
     * @param content long
     * @return long转为byte[] (小端)
     */
    public static byte[] long2BytesLE(long content) {
        byte[] result = new byte[8];
        result[7] = (byte) ((content >>> 56L) & 0xff);
        result[6] = (byte) ((content >>> 48L) & 0xff);
        result[5] = (byte) ((content >>> 40L) & 0xff);
        result[4] = (byte) ((content >>> 32L) & 0xff);
        result[3] = (byte) ((content >>> 24L) & 0xff);
        result[2] = (byte) ((content >>> 16L) & 0xff);
        result[1] = (byte) ((content >>> 8L) & 0xff);
        result[0] = (byte) (content & 0xff);
        return result;
    }

    /**
     * @param content long
     * @return long转为byte[] (大端)
     */
    public static byte[] long2Bytes(long content) {
        byte[] result = new byte[8];
        result[0] = (byte) ((content >>> 56L) & 0xff);
        result[1] = (byte) ((content >>> 48L) & 0xff);
        result[2] = (byte) ((content >>> 40L) & 0xff);
        result[3] = (byte) ((content >>> 32L) & 0xff);
        result[4] = (byte) ((content >>> 24L) & 0xff);
        result[5] = (byte) ((content >>> 16L) & 0xff);
        result[6] = (byte) ((content >>> 8L) & 0xff);
        result[7] = (byte) (content & 0xff);
        return result;
    }

    /**
     * @param content short
     * @return 翻转Short
     */
    public static short reverseBytesShort(short content) {
        return (short) ((content & 0xff00) >>> 8 | (content & 0x00ff) << 8);
    }

    /**
     * @param content int
     * @return 翻转Int
     */
    public static int reverseBytesInt(int content) {
        return (content & 0xff000000) >>> 24
                | (content & 0x00ff0000) >>> 8
                | (content & 0x0000ff00) << 8
                | (content & 0x000000ff) << 24;
    }

    /**
     * @param content long
     * @return 翻转Long
     */
    public static long reverseBytesLong(long content) {
        return (content & 0xff00000000000000L) >>> 56
                | (content & 0x00ff000000000000L) >>> 40
                | (content & 0x0000ff0000000000L) >>> 24
                | (content & 0x000000ff00000000L) >>> 8
                | (content & 0x00000000ff000000L) << 8
                | (content & 0x0000000000ff0000L) << 24
                | (content & 0x000000000000ff00L) << 40
                | (content & 0x00000000000000ffL) << 56;
    }
}
