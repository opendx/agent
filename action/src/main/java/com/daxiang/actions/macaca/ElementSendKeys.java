package com.daxiang.actions.macaca;

import macaca.client.commands.Element;


/**
 * Created by jiangyitao.
 */
public class ElementSendKeys {

    /**
     * 输入
     */
    public Object excute(Object element, Object content) throws Exception {
        Element _element = (Element) element;
        String _content = (String) content;

        _element.sendKeys(_content);
        return element;
    }
}
