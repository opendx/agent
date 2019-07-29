package com.daxiang.core.javacompile;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class FileManagerWrapper extends ForwardingJavaFileManager {
    private MemoryJavaFile file;

    public FileManagerWrapper(MemoryJavaFile file) {
        super(ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null));
        this.file = file;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        if (!file.getClassName().equals(className)) {
            throw new IOException("MemoryJavaFile:" + file.getClassName() + ",className:" + className);
        }
        return file;
    }
}
