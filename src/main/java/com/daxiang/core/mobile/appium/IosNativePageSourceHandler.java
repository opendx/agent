package com.daxiang.core.mobile.appium;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by jiangyitao.
 */
public class IosNativePageSourceHandler extends AppiumNativePageSourceHandler {

    /**
     * android / ios 复用一套前端inspector组件，将ios的布局转成android格式
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

        if ("AppiumAUT".equals(elementName)) {
            element.setName("hierarchy");
        } else {
            element.setName("node");

            Attribute xAttr = element.attribute("x");
            String startX = xAttr.getValue();
            element.remove(xAttr);

            Attribute yAttr = element.attribute("y");
            String startY = yAttr.getValue();
            element.remove(yAttr);

            Attribute widthAttr = element.attribute("width");
            String width = widthAttr.getValue();
            element.remove(widthAttr);

            Attribute heightAttr = element.attribute("height");
            String height = heightAttr.getValue();
            element.remove(heightAttr);

            Integer endX = Integer.parseInt(startX) + Integer.parseInt(width);
            Integer endY = Integer.parseInt(startY) + Integer.parseInt(height);

            String bounds = String.format("[%s,%s][%d,%d]", startX, startY, endX, endY);
            element.addAttribute("bounds", bounds);

            // 前端el-tree
            // defaultProps: {
            //   children: 'nodes',
            //   label: 'class'
            // }
            element.addAttribute("class", elementName);
        }

        List<Element> elements = element.elements();
        elements.forEach(e -> handleElement(e));
    }
}
