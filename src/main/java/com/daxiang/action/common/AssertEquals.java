package com.daxiang.action.common;

import org.junit.Assert;

/**
 * Created by jiangyitao.
 */
public class AssertEquals {

    public void excute(Object expected, Object actual) {
        Assert.assertEquals(expected, actual);
    }
}
