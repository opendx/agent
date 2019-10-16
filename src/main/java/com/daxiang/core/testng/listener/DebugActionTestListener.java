package com.daxiang.core.testng.listener;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * Created by jiangyitao.
 */
public class DebugActionTestListener extends TestListenerAdapter {

    /**
     * testng运行失败信息
     */
    public static ThreadLocal<String> failMsg = new ThreadLocal();

    /**
     * mobile.ftl print()
     */
    public static ThreadLocal<String> printMsg = new ThreadLocal<>();

    @Override
    public void onTestFailure(ITestResult tr) {
        failMsg.set(tr.getThrowable().getMessage());
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        failMsg.set(tr.getThrowable().getMessage());
    }
}
