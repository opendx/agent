package com.daxiang.core.mobile.android;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.daxiang.App;
import com.daxiang.core.PortProvider;
import com.daxiang.core.mobile.Mobile;
import com.daxiang.core.mobile.appium.AppiumNativePageSourceHandler;
import com.daxiang.model.page.Page;
import com.daxiang.server.ServerClient;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.core.mobile.android.scrcpy.Scrcpy;
import com.daxiang.core.mobile.android.scrcpy.ScrcpyVideoRecorder;
import com.daxiang.core.mobile.android.stf.AdbKit;
import com.daxiang.core.mobile.android.stf.Minicap;
import com.daxiang.core.mobile.android.stf.Minitouch;
import com.daxiang.core.mobile.appium.AndroidNativePageSourceHandler;
import com.daxiang.core.mobile.appium.AppiumServer;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

    public static final int PLATFORM = 1;
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
    }

    @Override
    public RemoteWebDriver newDriver() {
        return new AndroidDriver(deviceServer.getUrl(), caps);
    }

    @Override
    protected Capabilities newCaps(Capabilities capsToMerge) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true);
        capabilities.setCapability(AndroidMobileCapabilityType.UNICODE_KEYBOARD, true);
        capabilities.setCapability(AndroidMobileCapabilityType.RESET_KEYBOARD, true);
        capabilities.setCapability("recreateChromeDriverSessions", true);

        // 加速初始化速度
        capabilities.setCapability("skipServerInstallation", true);
        capabilities.setCapability("skipDeviceInitialization", true);
        capabilities.setCapability("skipUnlock", true);

        if (!greaterOrEqualsToAndroid5()) { // 小于安卓5，必须指定app，否则会创建driver失败
            capabilities.setCapability("appPackage", "io.appium.android.apis");
            capabilities.setCapability("appActivity", "io.appium.android.apis.ApiDemos");
        }

        // **** 以上capabilities可被传入的caps覆盖 ****

        capabilities.merge(capsToMerge);

        // **** 以下capabilities具有更高优先级，将覆盖传入的caps ****

        if (greaterOrEqualsToAndroid5()) {
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2); // UIAutomation2 is only supported since Android 5.0 (Lollipop)
        } else {
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UIAutomator1");
        }
        capabilities.setCapability(AndroidMobileCapabilityType.SYSTEM_PORT, PortProvider.getUiautomator2ServerAvailablePort());

        capabilities.setCapability("chromedriverPort", PortProvider.getAndroidChromeDriverAvailablePort());
        String chromedriverFilePath = getChromedriverFilePath();
        if (!StringUtils.isEmpty(chromedriverFilePath)) {
            capabilities.setCapability(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE, chromedriverFilePath);
        }

        capabilities.setCapability(MobileCapabilityType.UDID, getId());
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, mobile.getName());
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, mobile.getSystemVersion());
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, NEW_COMMAND_TIMEOUT);

        return capabilities;
    }

    public boolean greaterOrEqualsToAndroid5() {
        if (sdkVersion == null) {
            sdkVersion = AndroidUtil.getSdkVersion(iDevice); // 21 android5.0
        }
        return sdkVersion >= 21;
    }

    @Override
    public void installApp(String app) {
        boolean appIsUrl = true;
        try {
            new URL(app);
        } catch (MalformedURLException e) {
            appIsUrl = false;
        }

        if (appIsUrl) {
            try {
                File appFile = new File(UUIDUtil.getUUID());
                FileUtils.copyURLToFile(new URL(app), appFile);
                app = appFile.getAbsolutePath();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ScheduledExecutorService scheduleService = handleInstallBtnAsync();
        try {
            // 若使用appium安装，将导致handleInstallBtnAsync无法继续处理安装时的弹窗
            // 主要因为appium server无法在安装app时，响应其他请求，所以这里用ddmlib安装
            AndroidUtil.installApk(iDevice, app);
        } catch (InstallException e) {
            throw new RuntimeException(String.format("[%s]安装app失败: %s", getId(), e.getMessage()), e);
        } finally {
            if (!scheduleService.isShutdown()) {
                scheduleService.shutdown();
            }
            if (appIsUrl) {
                FileUtils.deleteQuietly(new File(app));
            }
        }
    }

    @Override
    public int getNativePageType() {
        return Page.TYPE_ANDROID_NATIVE;
    }

    @Override
    public AppiumNativePageSourceHandler newAppiumNativePageSourceHandler() {
        return new AndroidNativePageSourceHandler();
    }

    @Override
    public String getLogType() {
        return "logcat";
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
                log.warn("[{}]无法使用appium录制视频", getId(), e);
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
        String installBtnXpath = "//android.widget.Button[" +
                "contains(@text, '安装') " +
                "or contains(@text, '下一步') " +
                "or contains(@text, '确定') " +
                "or contains(@text, '确认')" +
                "]";
        By installBtnBy = By.xpath(installBtnXpath);

        ScheduledExecutorService scheduleService = Executors.newSingleThreadScheduledExecutor();
        scheduleService.scheduleAtFixedRate(() -> {
            try {
                driver.findElement(installBtnBy).click();
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
                log.info("[{}]download chromedriver from {}", getId(), downloadUrl);
                FileUtils.copyURLToFile(new URL(downloadUrl), chromedriverFile);

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
        // mobile重新插拔后，IDevice需要更新
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
