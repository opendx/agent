package com.daxiang.core.testng;

import com.daxiang.exception.ActionDebugException;
import org.springframework.util.CollectionUtils;
import org.testng.ITestNGListener;
import org.testng.TestNG;

import java.util.Arrays;
import java.util.List;

/**
 * Created by jiangyitao.
 */
public class TestNGRunner {

    public static void runTestCases(Class[] classes, Integer failRetryCount) {
        if (failRetryCount != null && failRetryCount > 0) {
            run(classes, Arrays.asList(TestCaseTestListener.class, FailRetryAnnotationTransformer.class));
        } else {
            run(classes, Arrays.asList(TestCaseTestListener.class));
        }
    }

    public static String debugAction(Class clazz) throws ActionDebugException {
        TestNG testNG = run(new Class[]{clazz}, Arrays.asList(DebugActionTestListener.class));
        if (testNG.getStatus() != 0) { // 运行有错误
            throw new ActionDebugException(DebugActionTestListener.getFailMsg());
        }

        List<String> printMsgList = DebugActionTestListener.getPrintMsgList();
        if (CollectionUtils.isEmpty(printMsgList)) {
            printMsgList = Arrays.asList("执行成功");
        }

        return String.join("\n", printMsgList);
    }

    private static TestNG run(Class[] testClasses, List<Class<? extends ITestNGListener>> listenerClasses) {
        TestNG testNG = new TestNG();
        testNG.setTestClasses(testClasses);
        testNG.setListenerClasses(listenerClasses);
        testNG.setUseDefaultListeners(false); // 不用默认监听器,不会自动生成testng的默认报告
        testNG.run();
        return testNG;
    }
}
