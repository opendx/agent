package com.daxiang.core.android;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.android.stf.AdbKit;
import com.daxiang.core.android.stf.Minicap;
import com.daxiang.core.android.stf.Minitouch;
import com.daxiang.core.appium.AndroidDriverBuilder;
import com.daxiang.core.appium.AndroidPageSourceHandler;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Device;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Data
public class AndroidDevice extends MobileDevice {

    public final static String TMP_FOLDER = "/data/local/tmp/";

    private IDevice iDevice;

    private Minicap minicap;
    private Minitouch minitouch;
    private AdbKit adbKit;

    public AndroidDevice(Device device, IDevice iDevice, AppiumServer appiumServer) {
        super(device, appiumServer);
        this.iDevice = iDevice;
    }

    public boolean canUseUiautomator2() {
        String androidVersion = getDevice().getSystemVersion();
        for (String sdkVersion : AndroidUtil.ANDROID_VERSION.keySet()) {
            if (androidVersion.equals(AndroidUtil.ANDROID_VERSION.get(sdkVersion))) {
                if (Integer.parseInt(sdkVersion) > 20) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        throw new RuntimeException("无法判断是否能用Uiautomator2，请更新AndroidUtil.ANDROID_VERSION");
    }

    @Override
    public AppiumDriver newDriver() {
        return new AndroidDriverBuilder().build(this);
    }

    @Override
    public File screenshot() throws Exception {
        return AndroidUtil.screenshotByMinicap(iDevice, getResolution());
    }

    @Override
    public void installApp(File appFile) throws InstallException {
        AndroidUtil.installApk(iDevice, appFile.getAbsolutePath());
    }

    @Override
    public String dump() throws IOException, DocumentException {
        return new AndroidPageSourceHandler(getAppiumDriver()).getPageSource();
    }

}
