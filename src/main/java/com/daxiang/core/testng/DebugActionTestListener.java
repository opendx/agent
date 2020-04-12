package com.daxiang.core.testng;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangyitao.
 */
public class DebugActionTestListener extends TestListenerAdapter {

    /**
     * testng运行失败信息
     */
    public static ThreadLocal<String> failMsg = new ThreadLocal();

    public static ThreadLocal<List<String>> printMsgList = new ThreadLocal<>();

    /**
     * mobile.ftl print()
     * @param msg
     */
    public static void addPrintMsg(String msg) {
        List<String> msgList = printMsgList.get();
        if (msgList == null) {
            msgList = new ArrayList<>();
            printMsgList.set(msgList);
        }

        msgList.add(msg);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        failMsg.set(tr.getThrowable().getMessage());
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        failMsg.set(tr.getThrowable().getMessage());
    }
}
