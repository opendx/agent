package com.daxiang.javacompile;

import lombok.extern.slf4j.Slf4j;

import javax.tools.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class JavaCompiler {

    public static Class compile(String className, String code) throws ClassNotFoundException, JavaCompileException {
        javax.tools.JavaCompiler systemJavaCompiler = ToolProvider.getSystemJavaCompiler();
        JavaFileManager javaFileManager = new ClassFileManager(systemJavaCompiler.getStandardFileManager(null, null, null));
        SimpleJavaFileObject simpleJavaFileObject = new JavaCode(className, code);
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector(); // 编译出错信息
        javax.tools.JavaCompiler.CompilationTask task = systemJavaCompiler.getTask(null, javaFileManager, diagnosticCollector, null, null, Arrays.asList(simpleJavaFileObject));

        log.info("[java编译]开始编译{}...", className);
        long start = System.currentTimeMillis();
        Boolean isCompileSuccess = task.call();
        log.info("[java编译]编译{}完成，编译是否成功：{}，耗时：{} ms", className, isCompileSuccess, System.currentTimeMillis() - start);

        if (!isCompileSuccess) {
            String compileFailInfo = diagnosticCollector.getDiagnostics().stream()
                    .map(diagnostic -> diagnostic.getMessage(null))
                    .collect(Collectors.joining("\n"));
            throw new JavaCompileException("编译失败: " + compileFailInfo);
        }

        ClassLoader classLoader = javaFileManager.getClassLoader(null);
        return classLoader.loadClass(className);
    }
}