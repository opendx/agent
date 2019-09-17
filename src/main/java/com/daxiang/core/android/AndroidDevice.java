package com.daxiang.core.android;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.daxiang.core.MinicapVideoRecorder;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.android.stf.AdbKit;
import com.daxiang.core.android.stf.Minicap;
import com.daxiang.core.android.stf.Minitouch;
import com.daxiang.core.appium.AndroidDriverBuilder;
import com.daxiang.core.appium.AndroidPageSourceHandler;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Device;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;

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

    private boolean canUseAppiumRecordVideo = true;
    private MinicapVideoRecorder minicapVideoRecorder;

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
    public AppiumDriver newAppiumDriver() {
        return new AndroidDriverBuilder().build(this, false);
    }

    @Override
    public AppiumDriver initAppiumDriver() {
        return new AndroidDriverBuilder().build(this, true);
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

    @Override
    public void startRecordingScreen() throws Exception {
        if (canUseAppiumRecordVideo) {
            try {
                AndroidStartScreenRecordingOptions androidOptions = new AndroidStartScreenRecordingOptions();
                // Since Appium 1.8.2 the time limit can be up to 1800 seconds (30 minutes).
                androidOptions.withTimeLimit(Duration.ofMinutes(30));
                androidOptions.withBitRate(200000); // default 4000000
                ((AndroidDriver) getAppiumDriver()).startRecordingScreen(androidOptions);
                return;
            } catch (Exception e) {
                log.warn("[{}]无法使用appium录制视频，改用minicap录制视频", getId(), e);
                canUseAppiumRecordVideo = false;
            }
        }

        // 使用minicap录屏
        minicapVideoRecorder = new MinicapVideoRecorder(this);
        minicapVideoRecorder.start();
    }

    @Override
    public File stopRecordingScreen() throws IOException {
        if (canUseAppiumRecordVideo) {
            File videoFile = new File(UUIDUtil.getUUID() + ".mp4");
            String base64Video = ((AndroidDriver) getAppiumDriver()).stopRecordingScreen();
            FileUtils.writeByteArrayToFile(videoFile, Base64.getDecoder().decode(base64Video), false);
            return videoFile;
        } else {
            return minicapVideoRecorder.stop();
        }
    }
}
