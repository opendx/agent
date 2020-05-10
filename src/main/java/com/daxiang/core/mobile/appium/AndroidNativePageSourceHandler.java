package com.daxiang.core.mobile.appium;

import org.dom4j.Element;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by jiangyitao.
 */
public class AndroidNativePageSourceHandler extends AppiumNativePageSourceHandler {

    /**
     * 由于appium pageSource返回的xml不是规范的xml，需要把除了hierarchy节点以外的节点替换成node，否则xml转json会出问题
     *
     * @param element
     */
    @Override
    public void handleElement(Element element) {
        if (element == null) {
            return;
        }

        String elementName = element.getName();
        if (StringUtils.isEmpty(elementName)) {
            return;
        }

        if (!"hierarchy".equals(elementName)) {
            element.setName("node");
        }

        List<Element> elements = element.elements();
        elements.forEach(e -> handleElement(e));
    }
}
