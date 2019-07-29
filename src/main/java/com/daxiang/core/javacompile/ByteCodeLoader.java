package com.daxiang.core.javacompile;

import java.security.SecureClassLoader;

/**
 * Created by jiangyitao.
 */
public class ByteCodeLoader extends SecureClassLoader {
    private final String className;
    private final byte[] byteCode;

    public ByteCodeLoader(String className, byte[] byteCode) {
        this.className = className;
        this.byteCode = byteCode;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!name.equals(className)) {
            throw new ClassNotFoundException(name);
        }

        return defineClass(name, byteCode, 0, byteCode.length);
    }

    public static Class<?> load(String className, byte[] byteCode) throws ClassNotFoundException {
        return new ByteCodeLoader(className, byteCode).loadClass(className);
    }
}
