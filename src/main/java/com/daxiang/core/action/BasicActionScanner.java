package com.daxiang.core.action;

import com.alibaba.fastjson.JSONArray;
import com.daxiang.model.action.Action;
import com.daxiang.model.action.Param;
import com.daxiang.model.action.PossibleValue;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    private PathMatchingResourcePatternResolver resourcePatternResolver;
    private MetadataReaderFactory metadataReaderFactory;

    public List<Action> scanRecursive(String basePackage) throws IOException, ClassNotFoundException {
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtils.convertClassNameToResourcePath(basePackage)
                + "/**/*.class";
        if (resourcePatternResolver == null) {
            resourcePatternResolver = new PathMatchingResourcePatternResolver();
        }
        Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);

        List<Action> actions = new ArrayList<>();

        if (metadataReaderFactory == null) {
            metadataReaderFactory = new CachingMetadataReaderFactory();
        }

        for (Resource resource : resources) {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
            Class clazz = Class.forName(metadataReader.getClassMetadata().getClassName());
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

        String methodName = method.getName();
        String actionName = StringUtils.isEmpty(actionAnno.name()) ? methodName : actionAnno.name();
        action.setName(actionName);

        action.setDescription(actionAnno.description());
        action.setType(Action.TYPE_BASE);

        // 静态方法使用className.methodName调用，否则使用$.methodName调用
        String actionInvoke = Modifier.isStatic(method.getModifiers())
                ? className + "." + methodName : "$." + methodName;
        action.setInvoke(actionInvoke);

        action.setReturnValueType(TypeUtils.toString(method.getGenericReturnType()));
        action.setReturnValueDesc(actionAnno.returnValueDesc());

        if (method.getAnnotation(Deprecated.class) != null) {
            // 废弃的action，添加到废弃分类
            action.setCategoryId(DEPRECATED_ACTION_CATEGORY_ID);
        }

        // -1为默认值
        if (actionAnno.projectId() != -1) {
            action.setProjectId(actionAnno.projectId());
        }

        action.setState(actionAnno.state());

        List<Param> params = Stream.of(method.getParameters()).map(parameter -> {
            Param param = new Param();
            param.setType(TypeUtils.toString(parameter.getParameterizedType()));
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
        action.setPlatforms(Ints.asList(actionAnno.platforms()));

        return action;
    }
}
