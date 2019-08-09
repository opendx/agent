package com.daxiang.core.testng;

import com.alibaba.fastjson.JSONObject;
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
    public String convert(String deviceId, String className, List<Action> testcases, String ftlBasePackagePath, String ftlFileName) throws IOException, TemplateException {
        List<Action> actionTreeList = new ArrayList<>();
        actionTreeList.addAll(testcases);

        Map<String, Object> dataModel = new HashMap();

        dataModel.put("testcases", testcases.stream().map(testcase -> {
            JSONObject tc = new JSONObject();
            tc.put("testcase", getCallMethodString(testcase));
            tc.put("id", testcase.getId());
            return tc;
        }).collect(Collectors.toList()));

        if (beforeClass != null) {
            actionTreeList.add(beforeClass);
            String callBeforeClass = getCallMethodString(beforeClass);
            dataModel.put("beforeClass", callBeforeClass);
        }
        if (afterClass != null) {
            actionTreeList.add(afterClass);
            String callAfterClass = getCallMethodString(afterClass);
            dataModel.put("afterClass", callAfterClass);
        }
        if (beforeMethod != null) {
            actionTreeList.add(beforeMethod);
            String callBeforeMethod = getCallMethodString(beforeMethod);
            dataModel.put("beforeMethod", callBeforeMethod);
        }
        if (afterMethod != null) {
            actionTreeList.add(afterMethod);
            String callAfterMethod = getCallMethodString(afterMethod);
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

        return FreemarkerUtil.process(ftlBasePackagePath, ftlFileName, dataModel);
    }

    /**
     * 获取调用方法的字符串。如在@Test下调用testcase的action，在@BeforeClass调用BeforeClass的action，等等...
     * todo /////////////xxxx
     * @param action
     * @return
     */
    private String getCallMethodString(Action action) {
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
        if (!CollectionUtils.isEmpty(actions)) {
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
                    String returnValue = action.getReturnValue();
                    if (returnValue.startsWith(Param.QUOTE_PREFIX) && returnValue.endsWith(Param.QUOTE_SUFFIX)) { // 方法参数
                        action.setReturnValue(Param.NAME_PREFIX + returnValue.substring(2, returnValue.length() - 1));
                    } else if (returnValue.startsWith(LocalVar.QUOTE_PREFIX) && returnValue.endsWith(LocalVar.QUOTE_SUFFIX)) { // 局部变量
                        action.setReturnValue(LocalVar.NAME_PREFIX + returnValue.substring(2, returnValue.length() - 1));
                    } else if (returnValue.startsWith(GlobalVar.QUOTE_PREFIX) && returnValue.endsWith(GlobalVar.QUOTE_SUFFIX)) { // 全局变量
                        action.setReturnValue(GlobalVar.NAME_PREFIX + returnValue.substring(2, returnValue.length() - 1));
                    } else { // 普通字符串
                        action.setReturnValue("\"" + returnValue + "\"");
                    }
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
                                String value = paramValue.getParamValue();
                                if (StringUtils.isEmpty(value)) {
                                    paramValue.setParamValue("null");
                                } else {
                                    if (value.startsWith(Param.QUOTE_PREFIX) && value.endsWith(Param.QUOTE_SUFFIX)) { // 方法参数
                                        paramValue.setParamValue(Param.NAME_PREFIX + value.substring(2, value.length() - 1));
                                    } else if (value.startsWith(LocalVar.QUOTE_PREFIX) && value.endsWith(LocalVar.QUOTE_SUFFIX)) { // 局部变量
                                        paramValue.setParamValue(LocalVar.NAME_PREFIX + value.substring(2, value.length() - 1));
                                    } else if (value.startsWith(GlobalVar.QUOTE_PREFIX) && value.endsWith(GlobalVar.QUOTE_SUFFIX)) { // 全局变量
                                        paramValue.setParamValue(GlobalVar.NAME_PREFIX + value.substring(2, value.length() - 1));
                                    } else { // 普通字符串
                                        paramValue.setParamValue("\"" + value + "\"");
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}
