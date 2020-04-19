package com.daxiang.model.page;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class Page {

    public static final int TYPE_ANDROID_NATIVE = 1;
    public static final int TYPE_IOS_NATIVE = 2;
    public static final int TYPE_WEB = 3;

    private Integer id;
    private String name;
    private Integer type;
    private List<Element> elements;
    private List<By> bys;
}