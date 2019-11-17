package com.daxiang.core.testng;

import com.daxiang.core.testng.listener.DebugActionTestListener;
import com.daxiang.core.testng.listener.TestCaseTestListener;
import com.daxiang.model.Response;
import org.springframework.util.CollectionUtils;
import org.testng.TestNG;

import java.util.Arrays;
import java.util.List;

/**
 * Created by jiangyitao.
 */
public class TestNGRunner {

    /**
     * 运行测试用例
     */
    public static void runTestCases(Class[] classes) {
        run(classes, Arrays.asList(TestCaseTestListener.class));
    }

    /**
     * 调试action
     */
    public static Response debugAction(Class clazz) {
        TestNG testNG = run(new Class[]{clazz}, Arrays.asList(DebugActionTestListener.class));
        if (testNG.getStatus() != 0) {
            // 运行有错误
            String failMsg = DebugActionTestListener.failMsg.get();
            DebugActionTestListener.failMsg.remove();
            return Response.fail(failMsg);
        } else {
            // 运行成功
            List<String> printMsgList = DebugActionTestListener.printMsgList.get();
            DebugActionTestListener.printMsgList.remove();
            if (CollectionUtils.isEmpty(printMsgList)) {
                printMsgList = Arrays.asList("执行成功");
            }
            return Response.success(printMsgList);
        }
    }

    private static TestNG run(Class[] testClasses, List<Class> listenerClasses) {
        TestNG testNG = new TestNG();
        testNG.setTestClasses(testClasses);
        testNG.setListenerClasses(listenerClasses);
        testNG.setUseDefaultListeners(false); // 不用默认监听器,不会自动生成testng的默认报告
        testNG.run();
        return testNG;
    }
}
