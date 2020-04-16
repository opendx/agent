package com.daxiang.core.android;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.daxiang.App;
import com.daxiang.server.ServerApi;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.android.scrcpy.Scrcpy;
import com.daxiang.core.android.scrcpy.ScrcpyVideoRecorder;
import com.daxiang.core.android.stf.AdbKit;
import com.daxiang.core.android.stf.Minicap;
import com.daxiang.core.android.stf.Minitouch;
import com.daxiang.core.appium.AndroidNativePageSourceHandler;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Device;
import com.daxiang.utils.HttpUtil;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.openqa.selenium.By;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Data
public class AndroidDevice extends MobileDevice {

    public static final String TMP_FOLDER = "/data/local/tmp";

    private IDevice iDevice;

    private Scrcpy scrcpy;
    private Minicap minicap;
    private Minitouch minitouch;
    private AdbKit adbKit;

    private ScrcpyVideoRecorder scrcpyVideoRecorder;

    private Integer sdkVersion;

    private boolean canUseAppiumRecordVideo = true;

    public AndroidDevice(Device device, IDevice iDevice, AppiumServer appiumServer) {
        super(device, appiumServer);
        this.iDevice = iDevice;
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

    public boolean greaterOrEqualsToAndroid5() {
        if (sdkVersion == null) {
            sdkVersion = AndroidUtil.getSdkVersion(iDevice); // 21 android5.0
        }
        return sdkVersion >= 21;
    }

    @Override
    public void installApp(File appFile) throws InstallException {
        ScheduledExecutorService service = handleInstallBtnAsync();
        try {
            AndroidUtil.installApk(iDevice, appFile.getAbsolutePath());
        } finally {
            FileUtils.deleteQuietly(appFile);
            if (!service.isShutdown()) {
                service.shutdown();
            }
        }
    }

    @Override
    public void uninstallApp(String app) throws InstallException {
        AndroidUtil.uninstallApk(iDevice, app);
    }

    @Override
    public String dump() throws IOException, DocumentException {
        return new AndroidNativePageSourceHandler(getAppiumDriver()).getPageSource();
    }

    @Override
    public boolean acceptAlert() {
        try {
            getAppiumDriver().executeScript("mobile:acceptAlert");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean dismissAlert() {
        try {
            getAppiumDriver().executeScript("mobile:dismissAlert");
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
                ((AndroidDriver) getAppiumDriver()).startRecordingScreen(androidOptions);
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
            String base64Video = ((AndroidDriver) getAppiumDriver()).stopRecordingScreen();
            FileUtils.writeByteArrayToFile(videoFile, Base64.getDecoder().decode(base64Video), false);
            return videoFile;
        } else {
            return scrcpyVideoRecorder.stop();
        }
    }

    /**
     * 处理安装app时弹窗
     */
    private ScheduledExecutorService handleInstallBtnAsync() {
        final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            try {
                String installBtnXpath = "//android.widget.Button[contains(@text, '安装') or contains(@text, '下一步') or contains(@text, '确定') or contains(@text, '确认')]";
                getAppiumDriver().findElement(By.xpath(installBtnXpath)).click();
            } catch (Exception ign) {
            }
        }, 0, 1, TimeUnit.SECONDS);
        return service;
    }

    public synchronized Optional<String> getChromedriverFilePath() {
        Optional<String> chromedriverDownloadUrl = ServerApi.getInstance().getChromedriverDownloadUrl(getId());
        if (!chromedriverDownloadUrl.isPresent()) {
            return Optional.empty();
        }

        String downloadUrl = chromedriverDownloadUrl.get();
        if (StringUtils.isEmpty(downloadUrl)) {
            return Optional.empty();
        }

        // 检查本地文件是否已存在
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
                return Optional.empty();
            }
        } else {
            log.info("[chromedriver][{}]file exist => {}", getId(), chromedriverFile.getAbsolutePath());
        }

        return Optional.of(chromedriverFile.getAbsolutePath());
    }
}
