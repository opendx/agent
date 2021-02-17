package com.daxiang.core.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jiangyitao.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action {
    int id();

    String name() default ""; // 默认为方法名

    String description() default "";

    int[] platforms() default {}; // 1.android 2.ios 3.web 空:所有平台通用

    int state() default 2; // 0.禁用 1.草稿 2.发布

    String returnValueDesc() default "";

    int projectId() default -1;
}
