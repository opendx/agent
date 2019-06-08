package com.fgnb.testng;

import com.alibaba.fastjson.JSONObject;
import com.fgnb.model.action.*;
import freemarker.template.TemplateException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

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
    private String deviceId;
    private Integer port;

    private Action beforeClass;
    private Action afterClass;
    private Action beforeMethod;
    private Action afterMethod;

    /**
     * 转换为testng代码
     */
    public String convert(String className, List<Action> testcases, String ftlBasePackagePath, String ftlFileName) throws IOException, TemplateException {
        Map<String, Object> dataModel = new HashMap();
        List<Action> actionTreeList = new ArrayList<>();

        actionTreeList.addAll(testcases);
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

        parseAction(actionTreeList);
        dataModel.put("actions", cachedActions.values());

        dataModel.put("className", className);
        dataModel.put("methodPrefix", METHOD_PREFIX);
        dataModel.put("globalVars", globalVars);
        dataModel.put("deviceId", deviceId);
        dataModel.put("port", port);
        dataModel.put("deviceTestTaskId", deviceTestTaskId);

        return FreemarkerUtil.process(ftlBasePackagePath, ftlFileName, dataModel);
    }

    /**
     * 获取调用方法的字符串。如在@Test下调用testcase的action，在@BeforeClass调用BeforeClass的action，等等...
     *
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
    private void parseAction(List<Action> actions) {
        for (Action action : actions) {
            Action cachedAction = cachedActions.get(action.getId());
            if (cachedAction == null) {
                List<Step> steps = action.getSteps();
                if (!CollectionUtils.isEmpty(steps)) {
                    for (Step step : steps) {
                        Action stepAction = step.getAction();
                        if (stepAction != null) {
                            parseAction(Arrays.asList(stepAction));
                        }
                    }
                }
                cachedActions.put(action.getId(), action);
            }
        }
    }
}
