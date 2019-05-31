package com.fgnb.testng;

import com.fgnb.model.action.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private String className;
    private Action actionTree;
    private String basePackagePath;
    private String ftlFileName;
    private Boolean isBeforeSuite;
    private Integer platform;

    /**
     * 转换为testng代码
     *
     * @return
     */
    public String convert() throws Exception {
        parseAction(actionTree);

        Map<String, Object> dataModel = new HashMap();
        dataModel.put("globalVars", globalVars);
        dataModel.put("actions", cachedActions.values());
        dataModel.put("className", className);
        dataModel.put("deviceId", deviceId);
        dataModel.put("port", port);
        dataModel.put("methodPrefix",METHOD_PREFIX);

        dataModel.put("deviceTestTaskId", deviceTestTaskId);
        dataModel.put("testcaseId", actionTree.getId());

        dataModel.put("isBeforeSuite", isBeforeSuite);
        dataModel.put("platform", platform);

        //testng @Test @BeforeSuite注解下调用的方法
        StringBuilder testMethod = new StringBuilder(METHOD_PREFIX + actionTree.getId() + "(");
        List<Param> actionParams = actionTree.getParams();
        //如果有参数 则都传入null
        if (!CollectionUtils.isEmpty(actionParams)) {
            testMethod.append(actionParams.stream().map(i -> "null").collect(Collectors.joining(",")));
        }
        testMethod.append(");");
        dataModel.put("testMethod", testMethod.toString());

        return FreemarkerUtil.process(basePackagePath, ftlFileName, dataModel);
    }

    /**
     * 递归遍历actionTree，把每个action放到cachedActions
     */
    private void parseAction(Action action) {
        Action cachedAction = cachedActions.get(action.getId());
        if (cachedAction == null) {
            List<Step> steps = action.getSteps();
            if(!CollectionUtils.isEmpty(steps)) {
                for(Step step : steps) {
                    Action stepAction = step.getAction();
                    if(stepAction != null) {
                        parseAction(stepAction);
                    }
                }
            }
            cachedActions.put(action.getId(), action);
        }
    }
}
