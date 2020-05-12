package com.daxiang.core.mobile.ios;

import com.daxiang.action.IosAction;
import com.daxiang.core.mobile.MobileTestNGCodeConverter;
import io.appium.java_client.ios.IOSDriver;

import java.util.Set;

/**
 * Created by jiangyitao.
 */
public class IosTestNGCodeConverter extends MobileTestNGCodeConverter {
    @Override
    protected Class getDriverClass() {
        return IOSDriver.class;
    }

    @Override
    protected Class getActionClass() {
        return IosAction.class;
    }

    @Override
    protected Class getDeviceClass() {
        return IosDevice.class;
    }

    @Override
    protected void addJavaImports(Set<String> javaImports) {
        super.addJavaImports(javaImports);
        javaImports.add("import com.daxiang.core.mobile.ios.IosDevice");
        javaImports.add("import io.appium.java_client.ios.IOSDriver");
        javaImports.add("import com.daxiang.action.IosAction");
    }

}
