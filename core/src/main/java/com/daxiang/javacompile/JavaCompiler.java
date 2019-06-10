package com.daxiang.javacompile;

import lombok.extern.slf4j.Slf4j;

import javax.tools.JavaFileManager;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.util.Arrays;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class JavaCompiler {

    public static Class compile(String className, String code) throws ClassNotFoundException {
        javax.tools.JavaCompiler systemJavaCompiler = ToolProvider.getSystemJavaCompiler();
        JavaFileManager javaFileManager = new ClassFileManager(systemJavaCompiler.getStandardFileManager(null, null, null));
        SimpleJavaFileObject simpleJavaFileObject = new JavaCode(className, code);
        javax.tools.JavaCompiler.CompilationTask task = systemJavaCompiler.getTask(null, javaFileManager, null, null, null, Arrays.asList(simpleJavaFileObject));

        log.info("[java编译]开始编译{}...", className);
        long start = System.currentTimeMillis();
        task.call();
        log.info("[java编译]编译{}完成，耗时: {} ms", className, System.currentTimeMillis() - start);

        ClassLoader classLoader = javaFileManager.getClassLoader(null);
        return classLoader.loadClass(className);
    }
}