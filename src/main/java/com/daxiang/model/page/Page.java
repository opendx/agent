package com.daxiang.model.page;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class Page {
    private Integer id;
    private String name;
    private List<Element> elements;
}