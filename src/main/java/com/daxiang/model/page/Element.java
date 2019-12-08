package com.daxiang.model.page;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class Element {
    private String name;
    // ["@AndroidFindBy", "uiAutomator"]
    private List<String> findBy;
    private String value;
}
