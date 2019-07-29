package com.daxiang.action.android;

import com.daxiang.core.android.AndroidDeviceHolder;
import com.daxiang.core.android.AndroidUtil;
import io.appium.java_client.AppiumDriver;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class RestartApk {

    private AppiumDriver driver;

    public RestartApk(AppiumDriver driver) {
        this.driver = driver;
    }

    public void excute(Object packageName, Object launchActivity) throws Exception {
        Assert.notNull(packageName, "包名不能为空");
        Assert.notNull(launchActivity, "启动Activity不能为空");

        String _packageName = (String) packageName;
        String _launchActivity = (String) launchActivity;

        AndroidUtil.restartApk(AndroidDeviceHolder.get(driver).getIDevice(), _packageName, _launchActivity);
    }
}
