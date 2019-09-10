package com.daxiang.action.appium.ios;

import com.daxiang.core.ios.IosUtil;
import io.appium.java_client.AppiumDriver;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class LaunchApp {

    private AppiumDriver appiumDriver;

    public LaunchApp(AppiumDriver appiumDriver) {
        this.appiumDriver = appiumDriver;
    }

    public void execute(Object bundleId) {
        Assert.notNull(bundleId, "bundleId不能为空");
        String _bundleId = (String) bundleId;
        IosUtil.launchApp(appiumDriver, _bundleId);
    }
}
