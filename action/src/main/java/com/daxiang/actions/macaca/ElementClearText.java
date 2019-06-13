package com.daxiang.actions.macaca;

import macaca.client.commands.Element;

/**
 * Created by jiangyitao.
 */
public class ElementClearText {

    /**
     * 清除文本
     */
    public Object excute(Object element) throws Exception {
        Element _element = (Element) element;
        _element.clearText();
        return element;
    }
}
