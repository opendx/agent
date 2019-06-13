package com.daxiang.actions.macaca;

import macaca.client.commands.Element;

/**
 * Created by jiangyitao.
 */
public class ClickElement {

    /**
     * 点击元素
     */
    public Object excute(Object element) throws Exception {
        Element _element = (Element) element;
        _element.click();
        return element;
    }
}
