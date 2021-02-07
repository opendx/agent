package com.daxiang.model.action;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class Action {

    public static final int TYPE_BASE = 1;

    private Integer id;
    private String name;
    private String description;
    private Integer type;
    private String invoke;
    private String returnValueType;
    private String returnValueDesc;
    private Integer categoryId;
    private Integer projectId;
    private Integer state;
    private List<Param> params;
    private List<LocalVar> localVars;
    private List<Step> setUp;
    private List<Step> steps;
    private List<Step> tearDown;
    private List<String> javaImports;
    private List<Action> importActions;
    private List<Integer> platforms;
    private List<Integer> depends;
}
