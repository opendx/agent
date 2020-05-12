package com.daxiang.core.mobile.android;

import com.daxiang.action.AndroidAction;
import com.daxiang.core.mobile.MobileTestNGCodeConverter;
import io.appium.java_client.android.AndroidDriver;

import java.util.Set;

/**
 * Created by jiangyitao.
 */
public class AndroidTestNGCodeConverter extends MobileTestNGCodeConverter {
    @Override
    protected Class getDriverClass() {
        return AndroidDriver.class;
    }

    @Override
    protected Class getActionClass() {
        return AndroidAction.class;
    }

    @Override
    protected Class getDeviceClass() {
        return AndroidDevice.class;
    }

    @Override
    protected void addJavaImports(Set<String> javaImports) {
        super.addJavaImports(javaImports);
        javaImports.add("import com.daxiang.core.mobile.android.AndroidDevice");
        javaImports.add("import io.appium.java_client.android.AndroidDriver");
        javaImports.add("import com.daxiang.action.AndroidAction");
    }

}
