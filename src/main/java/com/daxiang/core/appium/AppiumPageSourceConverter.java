package com.daxiang.core.appium;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.XML;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by jiangyitao.
 */
public class AppiumPageSourceConverter {


    public static String getJSONStringPageSource(AppiumDriver driver) throws IOException, DocumentException {
        if (driver == null) {
            throw new IllegalArgumentException("appium driver不能为空");
        }

        String pageSource = driver.getPageSource();
        if (StringUtils.isEmpty(pageSource)) {
            throw new IllegalArgumentException("pageSource为空");
        }

        try (InputStream in = new ByteArrayInputStream(pageSource.getBytes())) {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(in);
            Element rootElement = document.getRootElement();
            if (driver instanceof AndroidDriver) {
                handleAndroidNativeElement(rootElement);
            } else if (driver instanceof IOSDriver) {
                handleIosNativeElement(rootElement);
            } else {
                throw new RuntimeException("暂不支持的appium driver类型");
            }
            return XML.toJSONObject(document.asXML()).toString();
        }
    }

    /**
     * 由于appium pageSource返回的xml不是规范的xml，需要把除了hierarchy节点以外的节点替换成node，否则xml转json会出问题
     *
     * @param element
     */
    private static void handleAndroidNativeElement(Element element) {
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
        elements.forEach(e -> handleAndroidNativeElement(e));
    }

    /**
     * android / ios 复用一套前端inspector组件，将ios的布局转成android格式
     *
     * @param element
     */
    private static void handleIosNativeElement(Element element) {
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

            String endX = String.valueOf(Integer.parseInt(startX) + Integer.parseInt(width));
            String endY = String.valueOf(Integer.parseInt(startY) + Integer.parseInt(height));

            String bounds = String.format("[%s,%s][%s,%s]", startX, startY, endX, endY);
            element.addAttribute("bounds", bounds);

            // 前端el-tree
            // defaultProps: {
            //   children: 'nodes',
            //   label: 'class'
            // }
            element.addAttribute("class", elementName);
        }

        List<Element> elements = element.elements();
        elements.forEach(e -> handleIosNativeElement(e));
    }

}
