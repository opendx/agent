package com.daxiang.core.testng;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.action.common.ExecuteJavaCode;
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

        dataModel.put("executeJavaCodeActionId", ExecuteJavaCode.ID);

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
     * 递归把每个action放到cachedActions里
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
        cachedActions.remove(ExecuteJavaCode.ID);
    }

    /**
     * 在每个全局变量前加上前缀，防止和方法参数、局部变量冲突
     */
    private void handleGlobalVars() {
        if (!CollectionUtils.isEmpty(globalVars)) {
            globalVars.forEach(globalVar -> globalVar.setName(GlobalVar.NAME_PREFIX + globalVar.getName()));
        }
    }

    /**
     * 处理actions，方法参数、局部变量、返回值、步骤: 赋值、传入的参数
     */
    private void handleActions() {
        List<Action> actions = new ArrayList<>(cachedActions.values());
        for (Action action : actions) {
            // 在每个方法参数前加上前缀，防止和全局变量、局部变量冲突
            List<Param> params = action.getParams();
            if (!CollectionUtils.isEmpty(params)) {
                params.forEach(param -> param.setName(Param.NAME_PREFIX + param.getName()));
            }
            List<LocalVar> localVars = action.getLocalVars();
            // 在每个局部变量前加上前缀，防止和全局变量、方法参数冲突
            if (!CollectionUtils.isEmpty(localVars)) {
                localVars.forEach(localVar -> {
                    localVar.setName(LocalVar.NAME_PREFIX + localVar.getName());
                    if (StringUtils.isEmpty(localVar.getValue())) {
                        localVar.setValue("null");
                    } else {
                        localVar.setValue("\"" + localVar.getValue() + "\"");
                    }
                });
            }
            // 非基础Action有返回值时，可能是普通字符串 or 方法参数 or 局部变量 or 全局变量
            if (action.getType() != Action.TYPE_BASE && action.getHasReturnValue() == Action.HAS_RETURN_VALUE) {
                action.setReturnValue(handleValue(action.getReturnValue()));
            }
            // 步骤
            List<Step> steps = action.getSteps();
            if (!CollectionUtils.isEmpty(steps)) {
                steps.forEach(step -> {
                    String evaluation = step.getEvaluation();
                    // 处理赋值，只要赋值不为空一定是局部变量
                    if (!StringUtils.isEmpty(evaluation)) {
                        step.setEvaluation(LocalVar.NAME_PREFIX + evaluation.substring(2, evaluation.length() - 1));
                    }
                    // 处理步骤传入的参数值
                    List<ParamValue> paramValues = step.getParamValues();
                    if (!CollectionUtils.isEmpty(paramValues)) {
                        for (ParamValue paramValue : paramValues) {
                            if (step.getActionId() != ExecuteJavaCode.ID) {
                                paramValue.setParamValue(handleValue(paramValue.getParamValue()));
                            } else {
                                // 2019-10-02 直接嵌入代码，无需做处理
                                paramValue.setParamValue(paramValue.getParamValue());
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

        if (value.startsWith(Param.QUOTE_PREFIX) && value.endsWith(Param.QUOTE_SUFFIX)) { // 方法参数
            return Param.NAME_PREFIX + value.substring(2, value.length() - 1);
        } else if (value.startsWith(LocalVar.QUOTE_PREFIX) && value.endsWith(LocalVar.QUOTE_SUFFIX)) { // 局部变量
            return LocalVar.NAME_PREFIX + value.substring(2, value.length() - 1);
        } else if (value.startsWith(GlobalVar.QUOTE_PREFIX) && value.endsWith(GlobalVar.QUOTE_SUFFIX)) { // 全局变量
            return GlobalVar.NAME_PREFIX + value.substring(2, value.length() - 1);
        } else { // 普通字符串
            return "\"" + value + "\"";
        }
    }
}
