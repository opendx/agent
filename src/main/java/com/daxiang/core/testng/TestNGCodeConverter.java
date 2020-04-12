package com.daxiang.core.testng;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.action.appium.BasicAction;
import com.daxiang.model.action.*;
import com.daxiang.model.devicetesttask.DeviceTestTask;
import com.daxiang.model.devicetesttask.Testcase;
import freemarker.template.TemplateException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Data
@Accessors(chain = true)
public class TestNGCodeConverter {

    private static final String ACTION_PREFIX = "action_";
    private static final String TESTCASE_PREFIX = "testcase_";
    /**
     * actionId: Action
     */
    private final Map<Integer, Action> cachedActions = new HashMap();

    private final Set<String> javaImports = new HashSet<>();

    /**
     * 转换为testng代码
     */
    public String convert(DeviceTestTask deviceTestTask, String className, String ftlBasePackagePath, String ftlFileName) throws TestNGCodeConvertException {
        Map<String, Object> dataModel = new HashMap();
        List<Action> actionTreeList = new ArrayList<>();

        List<Testcase> testcases = deviceTestTask.getTestcases();
        actionTreeList.addAll(testcases);

        dataModel.put("testcases", testcases.stream().map(testcase -> {
            JSONObject tc = new JSONObject();
            tc.put("invoke", convertToInvokeMethodStringWithParamNull(testcase));
            tc.put("description", getDesc(deviceTestTask, testcase));
            tc.put("dependsOnMethods", getDependsOnMethods(testcase.getDepends()));
            tc.put("id", testcase.getId());
            return tc;
        }).collect(Collectors.toList()));

        Action beforeClass = deviceTestTask.getBeforeClass();
        if (beforeClass != null) {
            actionTreeList.add(beforeClass);
            String invokeBeforeClass = convertToInvokeMethodStringWithParamNull(beforeClass);
            dataModel.put("beforeClass", invokeBeforeClass);
        }

        Action afterClass = deviceTestTask.getAfterClass();
        if (afterClass != null) {
            actionTreeList.add(afterClass);
            String invokeAfterClass = convertToInvokeMethodStringWithParamNull(afterClass);
            dataModel.put("afterClass", invokeAfterClass);
        }

        Action beforeMethod = deviceTestTask.getBeforeMethod();
        if (beforeMethod != null) {
            actionTreeList.add(beforeMethod);
            String invokeBeforeMethod = convertToInvokeMethodStringWithParamNull(beforeMethod);
            dataModel.put("beforeMethod", invokeBeforeMethod);
        }

        Action afterMethod = deviceTestTask.getAfterMethod();
        if (afterMethod != null) {
            actionTreeList.add(afterMethod);
            String invokeAfterMethod = convertToInvokeMethodStringWithParamNull(afterMethod);
            dataModel.put("afterMethod", invokeAfterMethod);
        }

        parseActions(actionTreeList);

        handleActions();
        dataModel.put("actions", cachedActions.values());

        handleGlobalVars(deviceTestTask.getGlobalVars());

        dataModel.put("className", className);
        dataModel.put("actionPrefix", ACTION_PREFIX);
        dataModel.put("testcasePrefix", TESTCASE_PREFIX);
        dataModel.put("executeJavaCodeActionId", BasicAction.EXECUTE_JAVA_CODE_ID);

        handleJavaImports();
        dataModel.put("javaImports", javaImports);

        dataModel.put("deviceTestTask", deviceTestTask);

        try {
            return FreemarkerUtil.process(ftlBasePackagePath, ftlFileName, dataModel);
        } catch (IOException | TemplateException e) {
            throw new TestNGCodeConvertException(e);
        }
    }

    private String getDesc(DeviceTestTask deviceTestTask, Testcase testcase) {
        if (deviceTestTask.getId() == null) { // 调试
            return null;
        }

        return deviceTestTask.getDeviceId()
                + "_"
                + deviceTestTask.getId()
                + "_"
                + testcase.getId()
                + "_"
                + deviceTestTask.getTestPlan().getEnableRecordVideo()
                + "_"
                + deviceTestTask.getTestPlan().getFailRetryCount();
    }

    private String getDependsOnMethods(List<Integer> depends) {
        if (CollectionUtils.isEmpty(depends)) {
            return null;
        }

        // {"action_2","action_1"}
        return "{" + depends.stream().map(id -> "\"" + TESTCASE_PREFIX + id + "\"").collect(Collectors.joining(",")) + "}";
    }

    private void handleJavaImports() {
        javaImports.add("import com.daxiang.core.MobileDeviceHolder");
        javaImports.add("import io.appium.java_client.*");
        javaImports.add("import io.appium.java_client.pagefactory.*");
        javaImports.add("import org.testng.annotations.*");
        javaImports.add("import org.testng.SkipException");
        javaImports.add("import com.daxiang.core.testng.TestCaseTestListener");
        javaImports.add("import com.daxiang.core.testng.DebugActionTestListener");
        javaImports.add("import com.daxiang.action.appium.BasicAction");
        javaImports.add("import org.openqa.selenium.*");
        javaImports.add("import org.openqa.selenium.support.*");
        javaImports.add("import java.util.*");
        javaImports.add("import static org.assertj.core.api.Assertions.*");

        cachedActions.values().forEach(action -> {
            List<String> javaImports = action.getJavaImports();
            if (!CollectionUtils.isEmpty(javaImports)) {
                this.javaImports.addAll(javaImports);
            }
        });
    }

    /**
     * 转换Action为方法调用的字符串，如果需要传递参数则传入null
     *
     * @param action
     * @return
     */
    private String convertToInvokeMethodStringWithParamNull(Action action) {
        StringBuilder invokeMethod = new StringBuilder(ACTION_PREFIX + action.getId() + "(");
        List<Param> actionParams = action.getParams();
        // 如果有参数 则都传入null
        if (!CollectionUtils.isEmpty(actionParams)) {
            invokeMethod.append(actionParams.stream().map(i -> "null").collect(Collectors.joining(",")));
        }
        invokeMethod.append(");");
        return invokeMethod.toString();
    }

    /**
     * 递归把每个action放到cachedActions里，排除掉内嵌代码ExecuteJavaCode
     */
    private void parseActions(List<Action> actions) {
        for (Action action : actions) {
            Action cachedAction = cachedActions.get(action.getId());
            if (cachedAction == null) {
                // steps
                List<Step> steps = action.getSteps();
                if (!CollectionUtils.isEmpty(steps)) {
                    for (Step step : steps) {
                        Action stepAction = step.getAction();
                        if (stepAction != null) {
                            parseActions(Arrays.asList(stepAction));
                        }
                    }
                }

                // importActions
                List<Action> importActions = action.getImportActions();
                if (!CollectionUtils.isEmpty(importActions)) {
                    parseActions(importActions);
                }

                cachedActions.put(action.getId(), action);
            }
        }

        // 2019-10-02 新增内嵌代码ExecuteJavaCode，ExecuteJavaCode.ID是不需要调用的
        cachedActions.remove(BasicAction.EXECUTE_JAVA_CODE_ID);
    }

    /**
     * 处理全局变量值
     */
    private void handleGlobalVars(List<GlobalVar> globalVars) {
        if (!CollectionUtils.isEmpty(globalVars)) {
            globalVars.forEach(globalVar -> globalVar.setValue(handleValue(globalVar.getValue())));
        }
    }

    /**
     * 处理actions
     */
    private void handleActions() {
        List<Action> actions = new ArrayList<>(cachedActions.values());
        for (Action action : actions) {
            // 局部变量
            List<LocalVar> localVars = action.getLocalVars();
            if (!CollectionUtils.isEmpty(localVars)) {
                localVars.forEach(localVar -> localVar.setValue(handleValue(localVar.getValue())));
            }
            // 步骤
            List<Step> steps = action.getSteps();
            if (!CollectionUtils.isEmpty(steps)) {
                steps.forEach(step -> {
                    // 处理步骤传入的参数值
                    List<ParamValue> paramValues = step.getParamValues();
                    if (!CollectionUtils.isEmpty(paramValues)) {
                        for (ParamValue paramValue : paramValues) {
                            // 2019-10-02 ExecuteJavaCode直接嵌入代码，无需做处理
                            if (step.getActionId() != BasicAction.EXECUTE_JAVA_CODE_ID) {
                                paramValue.setParamValue(handleValue(paramValue.getParamValue()));
                            }
                        }
                    }
                });
            }
        }
    }

    private String handleValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return "null";
        }

        if (value.startsWith("${") && value.endsWith("}")) {
            return value.substring(2, value.length() - 1);
        } else { // 普通字符串
            return "\"" + value + "\"";
        }
    }
}
