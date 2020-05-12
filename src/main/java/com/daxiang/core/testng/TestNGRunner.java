package com.daxiang.core.testng;

import com.daxiang.model.Response;
import org.springframework.util.CollectionUtils;
import org.testng.ITestNGListener;
import org.testng.TestNG;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public static Response debugAction(Class clazz) {
        TestNG testNG = run(new Class[]{clazz}, Arrays.asList(DebugActionTestListener.class));
        if (testNG.getStatus() != 0) { // 运行有错误
            return Response.fail(DebugActionTestListener.getFailMsg());
        } else { // 运行成功
            List<String> printMsgList = DebugActionTestListener.getPrintMsgList();
            if (CollectionUtils.isEmpty(printMsgList)) {
                printMsgList = Arrays.asList("执行成功");
            }

            return Response.success(printMsgList.stream().collect(Collectors.joining("\n")));
        }
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
