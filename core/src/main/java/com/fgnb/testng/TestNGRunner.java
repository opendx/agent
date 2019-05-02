package com.fgnb.testng;

import com.fgnb.testng.listener.DebugActionTestListener;
import com.fgnb.testng.listener.TestListenerForTestCase;
import org.testng.TestNG;
import java.util.Arrays;

/**
 * Created by jiangyitao.
 */
public class TestNGRunner {

    /**
     * 运行测试用例
     *
     * @param classes
     */
    public static void runTestCasesByTestNG(Class[] classes) {
        TestNG testNG = new TestNG();
        testNG.setTestClasses(classes);
        testNG.setListenerClasses(Arrays.asList(TestListenerForTestCase.class));
        //不用默认监听器,则不会自动生成testng的默认报告
        testNG.setUseDefaultListeners(false);
        testNG.run();
    }

    /**
     * 调试action
     */
    public static void debugAction(Class clazz) {
        TestNG testNG = new TestNG();
        testNG.setTestClasses(new Class[]{clazz});
        testNG.setListenerClasses(Arrays.asList(DebugActionTestListener.class));
        //不用默认监听器,则不会自动生成testng的默认报告
        testNG.setUseDefaultListeners(false);
        testNG.run();

        if (testNG.getStatus() != 0) {
            //运行有错误
            String failInfo = DebugActionTestListener.failMsg.get();
            DebugActionTestListener.failMsg.remove();
            throw new RuntimeException(failInfo);
        }
    }
}
