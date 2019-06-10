package com.daxiang.javacompile;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * Created by jiangyitao.
 */
public class JavaClass extends SimpleJavaFileObject {
    private final ByteArrayOutputStream byteArrayOutputStream;

    public JavaClass(String className, Kind kind) {
        super(URI.create("string:///" + className.replace('.', '/') + kind.extension), kind);
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return byteArrayOutputStream;
    }

    public byte[] getClassBytes() {
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 重写finalize方法，在对象被回收时关闭输出流
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        byteArrayOutputStream.close();
    }
}
