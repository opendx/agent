package com.daxiang.core;

import com.daxiang.core.classloader.AgentExtJarLoader;
import lombok.extern.slf4j.Slf4j;
import org.dvare.dynamic.compiler.DynamicCompiler;
import org.dvare.dynamic.exceptions.DynamicCompilerException;
import org.springframework.util.Assert;


/**
 * Created by jiangyitao.
 */
@Slf4j
public class JavaCompiler {
    public static Class compile(String className, String code) throws DynamicCompilerException {
        Assert.hasLength(className, "className cannot be empty");
        Assert.hasLength(code, "code cannot be empty");

        ClassLoader classLoader = AgentExtJarLoader.getInstance().getClassLoader();
        DynamicCompiler dynamicCompiler = new DynamicCompiler(classLoader);
        dynamicCompiler.addSource(className, code);

        log.info("[java编译]开始编译{}...", className);
        long start = System.currentTimeMillis();
        Class clazz = dynamicCompiler.build().get(className);
        log.info("[java编译]编译{}成功, 耗时: {} ms", className, System.currentTimeMillis() - start);

        return clazz;
    }
}
