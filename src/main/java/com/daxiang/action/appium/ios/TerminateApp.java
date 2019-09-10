package com.daxiang.action.appium.ios;

import com.daxiang.core.ios.IosUtil;
import io.appium.java_client.AppiumDriver;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class TerminateApp {

    private AppiumDriver appiumDriver;

    public TerminateApp(AppiumDriver appiumDriver) {
        this.appiumDriver = appiumDriver;
    }

    public void execute(Object bundleId) {
        Assert.notNull(bundleId, "bundleId不能为空");
        String _bundleId = (String) bundleId;
        IosUtil.terminateApp(appiumDriver, _bundleId);
    }
}
