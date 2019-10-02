package com.daxiang.action.common;

import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class ExecuteJavaCode {

    /** common_2001-2500.sql 主键id: 2222（我的幸运数字）*/
    public static final int ID = 2222;

    /**
     * 这是一个特殊的action，
     * 转换为java代码时特殊处理该action
     *
     * @param javaCode
     */
    public void execute(Object javaCode) {
        Assert.notNull(javaCode, "javaCode不能为空");
    }
}
