package com.lm.utils.io;

import java.io.*;

/**
 * IO流工具类
 *
 * @author LM
 * @version 2019-05-16 v1.0.0
 */
public class IOUtils {
    /**
     * 关闭IO流，如果出现异常不打印错误堆栈
     *
     * @param closeables IO流
     */
    public static void closeIO(Closeable... closeables) {
        closeIO(false, closeables);
    }

    /**
     * 关闭IO流
     *
     * @param isPrintStackTrace 如果出现异常是否打印错误堆栈
     * @param closeables        IO流
     */
    public static void closeIO(boolean isPrintStackTrace, Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    if (isPrintStackTrace) e.printStackTrace();
                }
            }
        }
    }

    /**
     * 包装OutputStream
     *
     * @param outputStream OutputStream
     * @return OutputWrapper
     */
    public static OutputStreamWrapper outputStreamWrapper(OutputStream outputStream) {
        return new OutputStreamWrapper(outputStream);
    }

    /**
     * 包装OutputStream
     *
     * @param file   文件
     * @param append 是否添加模式
     * @return OutputWrapper
     * @throws FileNotFoundException 找不到文件异常
     */
    public static OutputStreamWrapper outputStreamWrapper(File file, boolean append) throws FileNotFoundException {
        return new OutputStreamWrapper(new FileOutputStream(file, append));
    }

    /**
     * 包装InputStream
     *
     * @param inputStream InputStream
     * @return InputStreamWrapper
     */
    public static InputStreamWrapper inputStreamWrapper(InputStream inputStream) {
        return new InputStreamWrapper(inputStream);
    }

    /**
     * 包装InputStream
     *
     * @param file 文件
     * @return InputStreamWrapper
     * @throws FileNotFoundException 找不到文件异常
     */
    public static InputStreamWrapper inputStreamWrapper(File file) throws FileNotFoundException {
        return new InputStreamWrapper(new FileInputStream(file));
    }
}
