package com.fgnb.testng;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by jiangyitao.
 */
@Data
public class Method {
    private String methodName;
    private String methodDescription;
    private List<String> methodParams;
    /** 局部变量 */
    private List<Map<String, String>> vars;
    private Boolean hasReturnValue;
    private String returnValue;
    private List<MethodStep> methodSteps;
    /** 基础action专用 needDriver */
    private Boolean needDriver;
    /** 基础action专用 className */
    private String className;
}
