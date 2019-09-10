package com.daxiang.action.appium.android;

import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidUtil;
import io.appium.java_client.AppiumDriver;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class ExecuteAdbShellCommond {

    private AppiumDriver driver;

    public ExecuteAdbShellCommond(AppiumDriver driver) {
        this.driver = driver;
    }

    public String execute(Object cmd) throws Exception {
        Assert.notNull(cmd, "命令不能为空");
        String _cmd = (String) cmd;

        return AndroidUtil.executeShellCommand(MobileDeviceHolder.getIDeviceByAppiumDriver(driver), _cmd);
    }
}
