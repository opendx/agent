package com.daxiang.core.mobile.appium;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by jiangyitao.
 */
public abstract class AppiumNativePageSourceHandler {

    abstract void handleElement(Element element);

    public String handle(String pageSource) throws IOException, DocumentException {
        if (StringUtils.isEmpty(pageSource)) {
            throw new IllegalArgumentException("pageSource cannot be empty");
        }

        try (InputStream in = new ByteArrayInputStream(pageSource.getBytes(Charset.forName("UTF-8")))) {
            SAXReader saxReader = new SAXReader();
            saxReader.setEncoding("UTF-8");

            Document document = saxReader.read(in);
            handleElement(document.getRootElement());

            return document.asXML();
        }
    }
}
