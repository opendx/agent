package com.daxiang.model.action;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class Param {
    private String type;
    private String name;
    private String description;
    private List<PossibleValue> possibleValues;
}

