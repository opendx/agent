package com.daxiang.core.mobile.android;

import com.alibaba.fastjson.JSONObject;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.daxiang.App;
import com.daxiang.core.mobile.Mobile;
import com.daxiang.core.mobile.appium.AppiumDriverFactory;
import com.daxiang.server.ServerClient;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.core.mobile.android.scrcpy.Scrcpy;
import com.daxiang.core.mobile.android.scrcpy.ScrcpyVideoRecorder;
import com.daxiang.core.mobile.android.stf.AdbKit;
import com.daxiang.core.mobile.android.stf.Minicap;
import com.daxiang.core.mobile.android.stf.Minitouch;
import com.daxiang.core.mobile.appium.AndroidNativePageSourceHandler;
import com.daxiang.core.mobile.appium.AppiumServer;
import com.daxiang.utils.HttpUtil;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AndroidDevice extends MobileDevice {

    public static final String TMP_FOLDER = "/data/local/tmp";

    private Integer sdkVersion;
    private IDevice iDevice;

    private Scrcpy scrcpy;
    private Minicap minicap;
    private Minitouch minitouch;
    private AdbKit adbKit;

    private boolean canUseAppiumRecordVideo = true;
    private ScrcpyVideoRecorder scrcpyVideoRecorder;

    public AndroidDevice(Mobile mobile, IDevice iDevice, AppiumServer appiumServer) {
        super(mobile, appiumServer);
        this.iDevice = iDevice;
        this.nativePageSourceHandler = new AndroidNativePageSourceHandler();
    }

    @Override
    public RemoteWebDriver newDriver(JSONObject caps) {
        return AppiumDriverFactory.createAndroidDriver(this, caps);
    }

    public boolean greaterOrEqualsToAndroid5() {
        if (sdkVersion == null) {
            sdkVersion = AndroidUtil.getSdkVersion(iDevice); // 21 android5.0
        }
        return sdkVersion >= 21;
    }

    @Override
    public void installApp(File appFile) {
        ScheduledExecutorService scheduleService = handleInstallBtnAsync();
        try {
            // 若使用appium安装，将导致handleInstallBtnAsync无法继续处理安装时的弹窗
            // 主要因为appium server无法在安装app时，响应其他请求，所以这里用ddmlib安装
            AndroidUtil.installApk(iDevice, appFile.getAbsolutePath());
        } catch (InstallException e) {
            throw new RuntimeException("安装app失败", e);
        } finally {
            if (!scheduleService.isShutdown()) {
                scheduleService.shutdown();
            }
        }
    }

    @Override
    public void uninstallApp(String app) throws InstallException {
        AndroidUtil.uninstallApk(iDevice, app);
    }

    @Override
    public boolean acceptAlert() {
        try {
            driver.executeScript("mobile:acceptAlert");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean dismissAlert() {
        try {
            driver.executeScript("mobile:dismissAlert");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void startRecordingScreen() {
        if (canUseAppiumRecordVideo) {
            try {
                AndroidStartScreenRecordingOptions androidOptions = new AndroidStartScreenRecordingOptions();
                // Since Appium 1.8.2 the time limit can be up to 1800 seconds (30 minutes).
                androidOptions.withTimeLimit(Duration.ofMinutes(30));
                androidOptions.withBitRate(Integer.parseInt(App.getProperty("androidRecordVideoBitRate")) * 1000000); // default 4000000
                ((AndroidDriver) driver).startRecordingScreen(androidOptions);
                return;
            } catch (Exception e) {
                log.warn("[{}]无法使用appium录制视频，改用scrcpy录制视频", getId(), e);
                canUseAppiumRecordVideo = false;
            }
        }

        if (scrcpyVideoRecorder == null) {
            scrcpyVideoRecorder = new ScrcpyVideoRecorder(getId());
        }
        scrcpyVideoRecorder.start();
    }

    @Override
    public File stopRecordingScreen() throws IOException {
        if (canUseAppiumRecordVideo) {
            File videoFile = new File(UUIDUtil.getUUID() + ".mp4");
            String base64Video = ((AndroidDriver) driver).stopRecordingScreen();
            FileUtils.writeByteArrayToFile(videoFile, Base64.getDecoder().decode(base64Video), false);
            return videoFile;
        } else {
            return scrcpyVideoRecorder.stop();
        }
    }

    /**
     * 异步处理安装app时弹窗
     */
    private ScheduledExecutorService handleInstallBtnAsync() {
        final ScheduledExecutorService scheduleService = Executors.newSingleThreadScheduledExecutor();
        scheduleService.scheduleAtFixedRate(() -> {
            try {
                String installBtnXpath = "//android.widget.Button[contains(@text, '安装') or contains(@text, '下一步') or contains(@text, '确定') or contains(@text, '确认')]";
                driver.findElement(By.xpath(installBtnXpath)).click();
            } catch (Exception ign) {
            }
        }, 0, 1, TimeUnit.SECONDS);
        return scheduleService;
    }

    public synchronized String getChromedriverFilePath() {
        String downloadUrl = ServerClient.getInstance().getChromedriverDownloadUrl(getId());
        if (StringUtils.isEmpty(downloadUrl)) {
            return null;
        }

        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
        File chromedriverFile = new File("vendor/driver/" + fileName);

        if (!chromedriverFile.exists()) {
            try {
                log.info("[chromedriver][{}]download => {}", getId(), downloadUrl);
                HttpUtil.downloadFile(downloadUrl, chromedriverFile);

                if (!Terminal.IS_WINDOWS) {
                    // 权限
                    Set<PosixFilePermission> permissions = new HashSet<>();
                    permissions.add(PosixFilePermission.OWNER_READ);
                    permissions.add(PosixFilePermission.OWNER_WRITE);
                    permissions.add(PosixFilePermission.OWNER_EXECUTE);
                    // 赋予权限
                    Files.setPosixFilePermissions(chromedriverFile.toPath(), permissions);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }

        return chromedriverFile.getAbsolutePath();
    }

    public IDevice getIDevice() {
        return iDevice;
    }

    public void setIDevice(IDevice iDevice) {
        // 设备重新插拔后，IDevice需要更新
        this.iDevice = iDevice;
        if (minicap != null) {
            minicap.setIDevice(iDevice);
        }
        if (minitouch != null) {
            minitouch.setIDevice(iDevice);
        }
        if (scrcpy != null) {
            scrcpy.setIDevice(iDevice);
        }
    }

    public Scrcpy getScrcpy() {
        return scrcpy;
    }

    public void setScrcpy(Scrcpy scrcpy) {
        this.scrcpy = scrcpy;
    }

    public Minicap getMinicap() {
        return minicap;
    }

    public void setMinicap(Minicap minicap) {
        this.minicap = minicap;
    }

    public Minitouch getMinitouch() {
        return minitouch;
    }

    public void setMinitouch(Minitouch minitouch) {
        this.minitouch = minitouch;
    }

    public AdbKit getAdbKit() {
        return adbKit;
    }

    public void setAdbKit(AdbKit adbKit) {
        this.adbKit = adbKit;
    }
}
