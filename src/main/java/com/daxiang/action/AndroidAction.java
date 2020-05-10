package com.daxiang.action;

import com.daxiang.core.mobile.android.AndroidDevice;
import com.daxiang.core.mobile.android.AndroidUtil;
import com.daxiang.core.mobile.android.IDeviceExecuteShellCommandException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 * id 2000 - 2999
 * platforms = [1]
 */
@Slf4j
public class AndroidAction extends MobileAction {

    private AndroidDevice androidDevice;

    public AndroidAction(AndroidDevice androidDevice) {
        super(androidDevice);
        this.androidDevice = androidDevice;
    }

    /**
     * 2000.清除apk数据
     */
    public void clearApkData(String packageName) throws IDeviceExecuteShellCommandException {
        Assert.hasText(packageName, "包名不能为空");

        AndroidUtil.clearApkData(androidDevice.getIDevice(), packageName);
    }

    /**
     * 2001.启动/重启 apk
     */
    public void restartApk(String packageName, String launchActivity) throws IDeviceExecuteShellCommandException {
        Assert.hasText(packageName, "包名不能为空");
        Assert.hasText(launchActivity, "启动Activity不能为空");

        AndroidUtil.restartApk(androidDevice.getIDevice(), packageName, launchActivity);
    }
}
