package com.daxiang.action.appium.android;

import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidUtil;
import io.appium.java_client.AppiumDriver;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class ClearApkData {

    private AppiumDriver driver;

    public ClearApkData(AppiumDriver driver) {
        this.driver = driver;
    }

    public void execute(Object packageName) throws Exception {
        Assert.notNull(packageName, "包名不能为空");
        String _packageName = (String) packageName;
        AndroidUtil.clearApkData(MobileDeviceHolder.getIDeviceByAppiumDriver(driver), _packageName);
    }
}
