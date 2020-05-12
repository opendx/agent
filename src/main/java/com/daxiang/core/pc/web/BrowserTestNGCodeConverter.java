package com.daxiang.core.pc.web;

import com.daxiang.action.PCWebAction;
import com.daxiang.core.testng.TestNGCodeConverter;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Set;

/**
 * Created by jiangyitao.
 */
public class BrowserTestNGCodeConverter extends TestNGCodeConverter {
    @Override
    protected Class getDriverClass() {
        return RemoteWebDriver.class;
    }

    @Override
    protected Class getActionClass() {
        return PCWebAction.class;
    }

    @Override
    protected Class getDeviceClass() {
        return BrowserDevice.class;
    }

    @Override
    protected void addJavaImports(Set<String> javaImports) {
        javaImports.add("import com.daxiang.core.pc.web.BrowserDevice");
        javaImports.add("import org.openqa.selenium.remote.RemoteWebDriver");
        javaImports.add("import com.daxiang.action.PCWebAction");
    }

}
