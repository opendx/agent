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
        Boolean compileSuccess = task.call();
        log.info("[java编译]编译{}完成，编译是否成功：{}，耗时：{} ms", className, compileSuccess, System.currentTimeMillis() - start);

        if (!compileSuccess) {
            throw new RuntimeException("编译失败，请检查action是否符合规范");
        }

        ClassLoader classLoader = javaFileManager.getClassLoader(null);
        return classLoader.loadClass(className);
    }
}