package com.fgnb.javacompile;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

/**
 * Created by jiangyitao.
 */
public class ClassFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private JavaClass javaClass;

    protected ClassFileManager(JavaFileManager javaFileManager) {
        super(javaFileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
        javaClass = new JavaClass(className, kind);
        return javaClass;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) {
                byte[] classBytes = javaClass.getClassBytes();
                return super.defineClass(name, classBytes, 0, classBytes.length);
            }
        };
    }
}
