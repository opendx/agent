package com.daxiang.core.appium;

import io.appium.java_client.AppiumDriver;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.XML;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jiangyitao.
 */
public abstract class AppiumPageSourceHandler {

    private AppiumDriver appiumDriver;

    public AppiumPageSourceHandler(AppiumDriver appiumDriver) {
        this.appiumDriver = appiumDriver;
    }

    abstract void handleElement(Element element);

    public String getPageSource() throws IOException, DocumentException {
        String pageSource = appiumDriver.getPageSource();
        if (StringUtils.isEmpty(pageSource)) {
            throw new RuntimeException("获取的PageSource为空");
        }

        try (InputStream in = new ByteArrayInputStream(pageSource.getBytes())) {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(in);
            Element rootElement = document.getRootElement();
            handleElement(rootElement);
            return XML.toJSONObject(document.asXML()).toString();
        }
    }
}
