package com.daxiang.model.page;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class By {
    private String name;
    // ["MobileBy", "id"]
    private List<String> findBy;
    private String value;
    private String description;
}
