package com.daxiang.action.appium.ios;

import com.daxiang.core.ios.IosUtil;
import io.appium.java_client.AppiumDriver;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class InstallApp {

    private AppiumDriver appiumDriver;

    public InstallApp(AppiumDriver appiumDriver) {
        this.appiumDriver = appiumDriver;
    }

    public void excute(Object appDownloadUrl) {
        Assert.notNull(appDownloadUrl, "appDownloadUrl不能为空");
        String _appDownloadUrl = (String) appDownloadUrl;
        IosUtil.installApp(appiumDriver, _appDownloadUrl);
    }
}
