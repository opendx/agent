package com.daxiang.core.testng;

import lombok.extern.slf4j.Slf4j;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class DebugActionTestListener extends TestListenerAdapter {

    private static final ThreadLocal<String> FAIL_MSG = new ThreadLocal<>();

    private static final ThreadLocal<List<String>> PRINT_MSG_LIST = new ThreadLocal<>();

    public static String getFailMsg() {
        String failMsg = FAIL_MSG.get();
        FAIL_MSG.remove();
        return failMsg;
    }

    public static List<String> getPrintMsgList() {
        List<String> printMsgList = PRINT_MSG_LIST.get();
        DebugActionTestListener.PRINT_MSG_LIST.remove();
        return printMsgList;
    }

    /**
     * codetemplate/index.ftl print()
     *
     * @param msg
     */
    public static void addPrintMsg(String msg) {
        List<String> msgList = PRINT_MSG_LIST.get();
        if (msgList == null) {
            msgList = new ArrayList<>();
            PRINT_MSG_LIST.set(msgList);
        }

        msgList.add(msg);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        log.error("debug fail", tr.getThrowable());
        FAIL_MSG.set(tr.getThrowable().getMessage());
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        log.error("debug fail", tr.getThrowable());
        FAIL_MSG.set(tr.getThrowable().getMessage());
    }
}
