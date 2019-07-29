package com.daxiang.core.javacompile;

import lombok.extern.slf4j.Slf4j;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class InMemoryJavaCompiler {
    public static Class compile(String className, String code) throws ClassNotFoundException, JavaCompileException {
        MemoryJavaFile file = new MemoryJavaFile(className, code);
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector(); // 编译出错信息
        JavaCompiler.CompilationTask task = ToolProvider.getSystemJavaCompiler().getTask(null, new FileManagerWrapper(file), diagnosticCollector, null, null, Arrays.asList(file));

        log.info("[java编译]开始编译{}", className);
        long start = System.currentTimeMillis();
        Boolean isCompileSuccess = task.call();
        log.info("[java编译]编译{}完成，编译是否成功：{}，耗时：{} ms", className, isCompileSuccess, System.currentTimeMillis() - start);

        if (!isCompileSuccess) {
            String compileFailInfo = diagnosticCollector.getDiagnostics().stream()
                    .map(diagnostic -> diagnostic.getMessage(null))
                    .collect(Collectors.joining("\n"));
            throw new JavaCompileException("编译失败: " + compileFailInfo);
        }

        return ByteCodeLoader.load(className, file.getByteCode());
    }
}
