package com.daxiang.javacompile;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * Created by jiangyitao.
 */
public class MemoryJavaFile extends SimpleJavaFileObject {

    private final String className;
    private final CharSequence code;
    private final ByteArrayOutputStream byteCode;

    public MemoryJavaFile(String className, CharSequence code) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.className = className;
        this.code = code;
        this.byteCode = new ByteArrayOutputStream();
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return byteCode;
    }

    public byte[] getByteCode() {
        return byteCode.toByteArray();
    }

    public String getClassName() {
        return className;
    }
}
