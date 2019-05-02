package com.fgnb.testng;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class MethodStep {
    /** 赋值 */
    private String evaluation;
    /** 被调用的方法名 */
    private String methodName;
    private Integer stepNumber;
    /** 被调用的步骤注释 */
    private String methodStepName;
    private List<String> methodParamValues;
}
