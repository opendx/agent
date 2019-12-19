package com.daxiang.model.action;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class Action {

    public static final Integer TYPE_BASE = 1;
    public static final Integer HAS_RETURN_VALUE = 1;

    private Integer id;
    private String name;
    private Integer type;
    private Integer platform;
    private String invoke;
    private String returnValue;
    private String returnValueDesc;
    private List<Param> params;
    private List<LocalVar> localVars;
    private List<Step> steps;
    private List<String> javaImports;
    private List<Integer> depends;
}