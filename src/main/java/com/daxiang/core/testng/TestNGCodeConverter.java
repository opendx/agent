package com.daxiang.core.testng;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.action.BaseAction;
import com.daxiang.model.action.*;
import com.daxiang.model.devicetesttask.DeviceTestTask;
import com.daxiang.model.devicetesttask.Testcase;
import freemarker.template.TemplateException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
public abstract class TestNGCodeConverter {

    private static final String FTL_BASE_PACKAGE_PATH = "/codetemplate";
    private static final String FTL_FILE_NAME = "index.ftl";

    private static final String ACTION_PREFIX = "action_";
    private static final String TESTCASE_PREFIX = "testcase_";
    /**
     * actionId: Action
     */
    private final Map<Integer, Action> cachedActions = new HashMap<>();

    private final Set<String> javaImports = new HashSet<>();

    /**
     * 转换为testng代码
     */
    public String convert(DeviceTestTask deviceTestTask, String className) throws TestNGCodeConvertException {
        Map<String, Object> dataModel = new HashMap<>();

        List<Testcase> testcases = deviceTestTask.getTestcases();
        dataModel.put("testcases", testcases.stream().map(testcase -> {
            JSONObject tc = new JSONObject();
            tc.put("invoke", getInvokeMethodStringWithDefaultParamValue(testcase));
            tc.put("description", getTestcaseDesc(deviceTestTask, testcase));
            tc.put("dependsOnMethods", getTestcaseDependsOnMethods(testcase.getDepends()));
            tc.put("id", testcase.getId());
            return tc;
        }).collect(Collectors.toList()));

        List<Action> actions = new ArrayList<>(testcases);

        Action beforeClass = deviceTestTask.getBeforeClass();
        if (beforeClass != null) {
            actions.add(beforeClass);
            String invokeBeforeClass = getInvokeMethodStringWithDefaultParamValue(beforeClass);
            dataModel.put("beforeClass", invokeBeforeClass);
        }

        Action afterClass = deviceTestTask.getAfterClass();
        if (afterClass != null) {
            actions.add(afterClass);
            String invokeAfterClass = getInvokeMethodStringWithDefaultParamValue(afterClass);
            dataModel.put("afterClass", invokeAfterClass);
        }

        Action beforeMethod = deviceTestTask.getBeforeMethod();
        if (beforeMethod != null) {
            actions.add(beforeMethod);
            String invokeBeforeMethod = getInvokeMethodStringWithDefaultParamValue(beforeMethod);
            dataModel.put("beforeMethod", invokeBeforeMethod);
        }

        Action afterMethod = deviceTestTask.getAfterMethod();
        if (afterMethod != null) {
            actions.add(afterMethod);
            String invokeAfterMethod = getInvokeMethodStringWithDefaultParamValue(afterMethod);
            dataModel.put("afterMethod", invokeAfterMethod);
        }

        parseActions(actions);
        cachedActions.remove(BaseAction.EXECUTE_JAVA_CODE_ID); // ExecuteJavaCode无需调用
        handleActionValue();
        dataModel.put("actions", cachedActions.values());

        handleGlobalVarValue(deviceTestTask.getGlobalVars());

        dataModel.put("className", className);
        dataModel.put("actionPrefix", ACTION_PREFIX);
        dataModel.put("testcasePrefix", TESTCASE_PREFIX);
        dataModel.put("executeJavaCodeActionId", BaseAction.EXECUTE_JAVA_CODE_ID);

        dataModel.put("driverClassSimpleName", getDriverClass().getSimpleName());
        dataModel.put("actionClassSimpleName", getActionClass().getSimpleName());
        dataModel.put("deviceClassSimpleName", getDeviceClass().getSimpleName());

        handleJavaImports();
        dataModel.put("javaImports", javaImports);

        dataModel.put("deviceTestTask", deviceTestTask);

        try {
            String code = FreemarkerUtil.process(FTL_BASE_PACKAGE_PATH, FTL_FILE_NAME, dataModel);

            IDocument doc = new Document(code);
            ToolFactory.createCodeFormatter(null)
                    .format(CodeFormatter.K_COMPILATION_UNIT, code, 0, code.length(), 0, null)
                    .apply(doc);

            return doc.get();
        } catch (IOException | TemplateException | BadLocationException e) {
            throw new TestNGCodeConvertException(e);
        }
    }

    protected abstract Class getDriverClass();

    protected abstract Class getActionClass();

    protected abstract Class getDeviceClass();

    protected abstract void addJavaImports(Set<String> javaImports);

    private String getTestcaseDesc(DeviceTestTask deviceTestTask, Testcase testcase) {
        if (deviceTestTask.getId() == null) { // 调试
            return null;
        }

        String deviceId = deviceTestTask.getDeviceId();
        Integer deviceTestTaskId = deviceTestTask.getId();
        Integer testcaseId = testcase.getId();
        Integer enableRecordVideo = deviceTestTask.getTestPlan().getEnableRecordVideo();
        Integer failRetryCount = deviceTestTask.getTestPlan().getFailRetryCount();

        return new TestDescription(deviceId, deviceTestTaskId, testcaseId,
                enableRecordVideo, failRetryCount).toString();
    }

    private String getTestcaseDependsOnMethods(List<Integer> depends) {
        if (CollectionUtils.isEmpty(depends)) {
            return null;
        }

        // {"action_2","action_1"}
        return "{" + depends.stream().map(id -> "\"" + TESTCASE_PREFIX + id + "\"").collect(Collectors.joining(",")) + "}";
    }

    private void handleJavaImports() {
        javaImports.add("import com.daxiang.core.testng.TestCaseTestListener");
        javaImports.add("import com.daxiang.core.testng.DebugActionTestListener");

        javaImports.add("import com.daxiang.core.Device");
        javaImports.add("import com.daxiang.core.DeviceHolder");

        javaImports.add("import org.testng.annotations.*");
        javaImports.add("import org.testng.SkipException");

        javaImports.add("import io.appium.java_client.pagefactory.*"); // AppiumFieldDecorator同样适用于pc web

        javaImports.add("import org.openqa.selenium.*");
        javaImports.add("import org.openqa.selenium.support.*");

        javaImports.add("import java.util.*");
        javaImports.add("import static org.assertj.core.api.Assertions.*");

        addJavaImports(javaImports);

        cachedActions.values().forEach(action -> {
            List<String> actionJavaImports = action.getJavaImports();
            if (!CollectionUtils.isEmpty(actionJavaImports)) {
                javaImports.addAll(actionJavaImports);
            }
        });
    }

    private String getInvokeMethodStringWithDefaultParamValue(Action action) {
        StringBuilder invokeMethod = new StringBuilder(ACTION_PREFIX + action.getId() + "(");
        List<Param> actionParams = action.getParams();
        if (!CollectionUtils.isEmpty(actionParams)) {
            invokeMethod.append(actionParams.stream()
                    .map(param -> getDefaultJavaTypeValue(param.getType()))
                    .collect(Collectors.joining(",")));
        }
        invokeMethod.append(");");
        return invokeMethod.toString();
    }

    /**
     * 递归把所有action放到cachedActions里
     */
    private void parseActions(List<Action> actions) {
        for (Action action : actions) {
            Action cachedAction = cachedActions.get(action.getId());
            if (cachedAction == null) {
                // steps
                parseSteps(action.getSetUp());
                parseSteps(action.getSteps());
                parseSteps(action.getTearDown());

                // importActions
                List<Action> importActions = action.getImportActions();
                if (!CollectionUtils.isEmpty(importActions)) {
                    parseActions(importActions);
                }

                cachedActions.put(action.getId(), action);
            }
        }
    }

    private void parseSteps(List<Step> steps) {
        if (CollectionUtils.isEmpty(steps)) {
            return;
        }

        for (Step step : steps) {
            Action stepAction = step.getAction();
            if (stepAction != null) {
                parseActions(Arrays.asList(stepAction));
            }
        }
    }

    /**
     * 处理globalVar value
     */
    private void handleGlobalVarValue(List<GlobalVar> globalVars) {
        if (!CollectionUtils.isEmpty(globalVars)) {
            globalVars.forEach(globalVar -> globalVar.setValue(handleValue(globalVar.getType(), globalVar.getValue())));
        }
    }

    /**
     * 处理action localVar value & step args
     */
    private void handleActionValue() {
        Collection<Action> actions = cachedActions.values();
        for (Action action : actions) {
            handleLocalVarValue(action.getLocalVars());
            handleStepArgs(action.getSetUp());
            handleStepArgs(action.getSteps());
            handleStepArgs(action.getTearDown());
        }
    }

    private void handleLocalVarValue(List<LocalVar> localVars) {
        if (!CollectionUtils.isEmpty(localVars)) {
            localVars.forEach(localVar -> localVar.setValue(handleValue(localVar.getType(), localVar.getValue())));
        }
    }

    private void handleStepArgs(List<Step> steps) {
        if (CollectionUtils.isEmpty(steps)) {
            return;
        }

        for (Step step : steps) {
            Integer stepActionId = step.getActionId();
            // ExecuteJavaCode直接嵌入模版，无需处理
            if (stepActionId == BaseAction.EXECUTE_JAVA_CODE_ID) {
                continue;
            }

            List<Param> stepActionParams = cachedActions.get(stepActionId).getParams();
            if (CollectionUtils.isEmpty(stepActionParams)) {
                step.setArgs(new ArrayList<>(0));
                continue;
            }

            List<String> args = step.getArgs();
            List<String> newArgs = new ArrayList<>(stepActionParams.size()); // 以actionParam为准

            for (int i = 0; i < stepActionParams.size(); i++) {
                String type = stepActionParams.get(i).getType();
                if (args != null && i < args.size()) {
                    newArgs.add(i, handleValue(type, args.get(i)));
                } else {
                    newArgs.add(i, getDefaultJavaTypeValue(type));
                }
            }

            step.setArgs(newArgs);
        }
    }

    private String handleValue(String type, String value) {
        if (StringUtils.isEmpty(value)) {
            return getDefaultJavaTypeValue(type);
        }

        if (value.startsWith("${") && value.endsWith("}")) {
            return value.substring(2, value.length() - 1);
        }

        if ("String".equals(type) || "java.lang.String".equals(type)) {
            return "\"" + value + "\"";
        }

        return value;
    }

    private String getDefaultJavaTypeValue(String type) {
        if ("byte".equals(type) || "short".equals(type) || "int".equals(type)
                || "long".equals(type) || "float".equals(type) || "double".equals(type)) {
            return "0";
        } else if ("char".equals(type)) {
            return "'\\u0000'";
        } else if ("boolean".equals(type)) {
            return "false";
        } else {
            return "null";
        }
    }
}
