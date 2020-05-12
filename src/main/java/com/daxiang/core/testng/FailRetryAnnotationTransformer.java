package com.daxiang.core.testng;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by jiangyitao.
 */
public class FailRetryAnnotationTransformer implements IAnnotationTransformer {
    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        Class retry = annotation.getRetryAnalyzerClass();
        if (retry != FailRetryAnalyzer.class) {
            annotation.setRetryAnalyzer(FailRetryAnalyzer.class);
        }
    }
}
