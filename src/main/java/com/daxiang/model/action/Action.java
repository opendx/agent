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
    private Integer type;
    private List<Integer> platforms;
    private String invoke;
    private String returnValueType;
    private String returnValueDesc;
    private List<Param> params;
    private List<LocalVar> localVars;
    private List<Step> steps;
    private List<String> javaImports;
    private List<Action> importActions;
    private List<Integer> depends;
    private Integer categoryId;
    private Integer projectId;
    private Integer state;
}
