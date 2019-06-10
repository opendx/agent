package com.daxiang.javacompile;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * Created by jiangyitao.
 */
public class JavaCode extends SimpleJavaFileObject {
    private final String code;

    public JavaCode(String className, String code) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public String getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}
