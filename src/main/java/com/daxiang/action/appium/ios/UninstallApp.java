package com.daxiang.action.appium.ios;

import com.daxiang.core.ios.IosUtil;
import io.appium.java_client.AppiumDriver;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class UninstallApp {

    private AppiumDriver appiumDriver;

    public UninstallApp(AppiumDriver appiumDriver) {
        this.appiumDriver = appiumDriver;
    }

    public void excute(Object bundleId) {
        Assert.notNull(bundleId, "bundleId不能为空");
        String _bundleId = (String) bundleId;
        IosUtil.uninstallApp(appiumDriver, _bundleId);
    }
}
