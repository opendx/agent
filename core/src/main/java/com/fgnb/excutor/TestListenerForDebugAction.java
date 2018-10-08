package com.fgnb.excutor;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * Created by jiangyitao.
 */
public class TestListenerForDebugAction extends TestListenerAdapter{

    public static ThreadLocal<String> failInfo = new ThreadLocal();

    @Override
    public void onTestFailure(ITestResult tr) {
        failInfo.set(tr.getThrowable().getMessage());
    }

}
