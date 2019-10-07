package com.daxiang.core.testng;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.action.appium.BasicAction;
import com.daxiang.model.action.*;
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

    private static final String METHOD_PREFIX = "action_";
    /**
     * actionId: Action
     */
    private final Map<Integer, Action> cachedActions = new HashMap();

    private Integer deviceTestTaskId;
    private List<GlobalVar> globalVars;

    private Action beforeClass;
    private Action afterClass;
    private Action beforeMethod;
    private Action afterMethod;

    /**
     * 转换为testng代码
     */
    public String convert(String deviceId, String className, List<? extends Action> testcases,
                          String ftlBasePackagePath, String ftlFileName) throws IOException, TemplateException {
        List<Action> actionTreeList = new ArrayList<>();
        actionTreeList.addAll(testcases);

        Map<String, Object> dataModel = new HashMap();

        dataModel.put("testcases", testcases.stream().map(testcase -> {
            JSONObject tc = new JSONObject();
            tc.put("testcase", convertToInvokeMethodStringWithParamNull(testcase));
            tc.put("id", testcase.getId());
            return tc;
        }).collect(Collectors.toList()));

        if (beforeClass != null) {
            actionTreeList.add(beforeClass);
            String callBeforeClass = convertToInvokeMethodStringWithParamNull(beforeClass);
            dataModel.put("beforeClass", callBeforeClass);
        }
        if (afterClass != null) {
            actionTreeList.add(afterClass);
            String callAfterClass = convertToInvokeMethodStringWithParamNull(afterClass);
            dataModel.put("afterClass", callAfterClass);
        }
        if (beforeMethod != null) {
            actionTreeList.add(beforeMethod);
            String callBeforeMethod = convertToInvokeMethodStringWithParamNull(beforeMethod);
            dataModel.put("beforeMethod", callBeforeMethod);
        }
        if (afterMethod != null) {
            actionTreeList.add(afterMethod);
            String callAfterMethod = convertToInvokeMethodStringWithParamNull(afterMethod);
            dataModel.put("afterMethod", callAfterMethod);
        }

        parseActions(actionTreeList);

        handleActions();
        dataModel.put("actions", cachedActions.values());

        handleGlobalVars();
        dataModel.put("globalVars", globalVars);

        dataModel.put("className", className);
        dataModel.put("methodPrefix", METHOD_PREFIX);
        dataModel.put("deviceId", deviceId);
        dataModel.put("deviceTestTaskId", deviceTestTaskId);

        dataModel.put("executeJavaCodeActionId", BasicAction.EXECUTE_JAVA_CODE_ID);

        return FreemarkerUtil.process(ftlBasePackagePath, ftlFileName, dataModel);
    }

    /**
     * 转换Action为方法调用的字符串，如果需要传递参数则传入null
     *
     * @param action
     * @return
     */
    private String convertToInvokeMethodStringWithParamNull(Action action) {
        StringBuilder callMethodString = new StringBuilder(METHOD_PREFIX + action.getId() + "(");
        List<Param> actionParams = action.getParams();
        // 如果有参数 则都传入null
        if (!CollectionUtils.isEmpty(actionParams)) {
            callMethodString.append(actionParams.stream().map(i -> "null").collect(Collectors.joining(",")));
        }
        callMethodString.append(");");
        return callMethodString.toString();
    }

    /**
     * 递归把每个action放到cachedActions里，排除掉内嵌代码ExecuteJavaCode
     */
    private void parseActions(List<Action> actions) {
        for (Action action : actions) {
            Action cachedAction = cachedActions.get(action.getId());
            if (cachedAction == null) {
                List<Step> steps = action.getSteps();
                if (!CollectionUtils.isEmpty(steps)) {
                    for (Step step : steps) {
                        Action stepAction = step.getAction();
                        if (stepAction != null) {
                            parseActions(Arrays.asList(stepAction));
                        }
                    }
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
    private void handleGlobalVars() {
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
