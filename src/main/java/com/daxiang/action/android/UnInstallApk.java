package com.daxiang.action.android;

import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidUtil;
import io.appium.java_client.AppiumDriver;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class UnInstallApk {

    private AppiumDriver driver;

    public UnInstallApk(AppiumDriver driver) {
        this.driver = driver;
    }

    public void excute(Object packageName) throws Exception {
        Assert.notNull(packageName, "apk包名不能为空");

        String _packageName = (String) packageName;
        AndroidUtil.uninstallApk(MobileDeviceHolder.getIDeviceByAppiumDriver(driver), _packageName);
    }
}
