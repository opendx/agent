package com.daxiang.action;

import com.daxiang.core.mobile.android.AndroidDevice;
import com.daxiang.core.mobile.android.AndroidUtil;
import com.daxiang.core.mobile.android.IDeviceExecuteShellCommandException;
import io.appium.java_client.android.AndroidDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 * id 2000 - 2999
 * platforms = [1]
 */
@Slf4j
public class AndroidAction extends MobileAction {

    private AndroidDriver driver;

    public AndroidAction(AndroidDriver driver) {
        super(driver);
        this.driver = driver;
    }

    /**
     * 2000.清除apk数据
     */
    public void clearApkData(String packageName) throws IDeviceExecuteShellCommandException {
        Assert.hasText(packageName, "包名不能为空");

        AndroidUtil.clearApkData(((AndroidDevice) mobileDevice).getIDevice(), packageName);
    }

    /**
     * 2001.启动/重启 apk
     */
    public void restartApk(String packageName, String launchActivity) throws IDeviceExecuteShellCommandException {
        Assert.hasText(packageName, "包名不能为空");
        Assert.hasText(launchActivity, "启动Activity不能为空");

        AndroidUtil.restartApk(((AndroidDevice) mobileDevice).getIDevice(), packageName, launchActivity);
    }
}
