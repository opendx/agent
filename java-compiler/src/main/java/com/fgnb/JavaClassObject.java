package com.fgnb;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * 将输出流交给JavaCompiler，最后JavaCompiler将编译后的class文件写入输出流中
 */
public class JavaClassObject extends SimpleJavaFileObject {

    /**
     * 定义一个输出流，用于装载JavaCompiler编译后的Class文件
     */
    protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    /**
     * 调用父类构造器
     * @param name
     * @param kind
     */
    public JavaClassObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

    /**
     * 获取输出流为byte[]数组
     * @return
     */
    public byte[] getBytes() {
        return bos.toByteArray();
    }

    /**
     * 重写openOutputStream，将我们的输出流交给JavaCompiler，让它将编译好的Class装载进来
     * @return
     * @throws IOException
     */
    @Override
    public OutputStream openOutputStream() throws IOException {
        return bos;
    }

    /**
     * 重写finalize方法，在对象被回收时关闭输出流
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        bos.close();
    }
}