package com.fgnb.excutor;

import com.fgnb.DynamicEngine;
import org.testng.TestNG;

import java.util.Arrays;

/**
 * Created by jiangyitao.
 */
public class Excutor {

    /**
     * 编译
     * @param fullClassName
     * @param code
     */
    public Class compiler(String fullClassName,String code) throws Exception{
         return DynamicEngine.getInstance().javaCodeToClass(fullClassName, code);
    }

    /**
     * 运行测试用例
     * @param classes
     */
    public void runTestCasesByTestNG(Class[] classes){
        TestNG testNG = new TestNG();
        //测试类
        testNG.setTestClasses(classes);
        //测试用例监听器
        testNG.setListenerClasses(Arrays.asList(TestListenerForTestCase.class));
        //不用默认监听器 则不会自动生成testng的默认报告
        testNG.setUseDefaultListeners(false);
        //运行
        testNG.run();
    }
    /**
     * 调试action
     * @param classes
     * @throws Exception
     */
    public void debugActionByTestNG(Class[] classes) throws Exception{
        TestNG testNG = new TestNG();
        //测试类
        testNG.setTestClasses(classes);
        //测试监听器
        testNG.setListenerClasses(Arrays.asList(TestListenerForDebugAction.class));
        //不用默认监听器 则不会自动生成testng的默认报告
        testNG.setUseDefaultListeners(false);
        //运行
        testNG.run();

        if(testNG.getStatus() != 0){
            //运行用例有错误
            String failInfo = TestListenerForDebugAction.failInfo.get();
            TestListenerForDebugAction.failInfo.remove();
            throw new Exception(failInfo);
        }
    }

}
