package com.daxiang.core.mobile;

import com.daxiang.core.testng.TestNGCodeConverter;

import java.util.Set;

/**
 * Created by jiangyitao.
 */
public abstract class MobileTestNGCodeConverter extends TestNGCodeConverter {
    @Override
    protected void addJavaImports(Set<String> javaImports) {
        javaImports.add("import io.appium.java_client.*");
    }
}
