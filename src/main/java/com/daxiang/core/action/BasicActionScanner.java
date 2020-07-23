package com.daxiang.core.action;

import com.alibaba.fastjson.JSONArray;
import com.daxiang.model.action.Action;
import com.daxiang.model.action.Param;
import com.daxiang.model.action.PossibleValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.google.common.reflect.ClassPath;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jiangyitao.
 */
public class BasicActionScanner {

    // 废弃的action分类id
    private static final int DEPRECATED_ACTION_CATEGORY_ID = 10000;
    // basic action最大id
    private static final int BASIC_ACTION_MAX_ID = 10000;

    public List<Action> scan(String packageName) throws IOException {
        Assert.hasText(packageName, "packageName must has text");

        List<Action> actions = new ArrayList<>();

        // 扫描packageName目录及子目录
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ImmutableSet<ClassPath.ClassInfo> classInfos = ClassPath.from(classLoader)
                .getTopLevelClassesRecursive(packageName);

        for (ClassPath.ClassInfo classInfo : classInfos) {
            Class clazz = classInfo.load();
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                Action action = createAction(clazz.getName(), method);
                if (action != null) {
                    actions.add(action);
                }
            }
        }

        return actions;
    }

    private Set<Integer> cachedActionIds = new HashSet<>();

    private Action createAction(String className, Method method) {
        com.daxiang.core.action.annotation.Action actionAnno = method
                .getAnnotation(com.daxiang.core.action.annotation.Action.class);
        // 只创建带action注解的方法
        if (actionAnno == null) {
            return null;
        }

        int actionId = actionAnno.id();
        if (actionId > BASIC_ACTION_MAX_ID) {
            throw new RuntimeException(String.format("actionId: %d不能大于%d", actionId, BASIC_ACTION_MAX_ID));
        }
        if (cachedActionIds.contains(actionId)) {
            throw new RuntimeException(String.format("actionId: %d重复", actionId));
        }
        cachedActionIds.add(actionId);

        Action action = new Action();
        action.setId(actionId);
        action.setType(Action.TYPE_BASE);
        action.setPlatforms(Ints.asList(actionAnno.platforms()));
        action.setReturnValueType(method.getReturnType().getSimpleName());

        if (method.getAnnotation(Deprecated.class) != null) {
            // 废弃的action，添加到废弃分类
            action.setCategoryId(DEPRECATED_ACTION_CATEGORY_ID);
        }

        String methodName = method.getName();
        String actionName = StringUtils.isEmpty(actionAnno.name()) ? methodName : actionAnno.name();
        action.setName(actionName);

        // 默认使用$.methodName调用，否则使用全类名.methodName调用
        String actionInvoke = actionAnno.invoke() == 1 ? "$." + methodName : className + "." + methodName;
        action.setInvoke(actionInvoke);

        List<Param> params = Stream.of(method.getParameters()).map(parameter -> {
            Param param = new Param();
            param.setType(parameter.getType().getSimpleName());
            param.setName(parameter.getName());

            com.daxiang.core.action.annotation.Param paramAnno = parameter
                    .getAnnotation(com.daxiang.core.action.annotation.Param.class);
            if (paramAnno != null) {
                param.setDescription(paramAnno.description());
                List<PossibleValue> possibleValues = JSONArray.parseArray(paramAnno.possibleValues(), PossibleValue.class);
                param.setPossibleValues(possibleValues);
            }

            return param;
        }).collect(Collectors.toList());
        action.setParams(params);

        return action;
    }
}
