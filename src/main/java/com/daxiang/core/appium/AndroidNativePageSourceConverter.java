package com.daxiang.core.appium;

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
public class AndroidNativePageSourceConverter {


    public static String convert(String pageSource) throws IOException, DocumentException {
        if (StringUtils.isEmpty(pageSource)) {
            throw new IllegalArgumentException("pageSource不能为空");
        }

        // 由于appium pageSource返回的xml不是规范的xml，需要把除了hierarchy节点以外的节点替换成node，否则xml转json会出问题
        try (InputStream in = new ByteArrayInputStream(pageSource.getBytes())) {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(in);
            Element rootElement = document.getRootElement();
            handleElement(rootElement);
            return XML.toJSONObject(document.asXML()).toString();
        }
    }

    private static void handleElement(Element element) {
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
