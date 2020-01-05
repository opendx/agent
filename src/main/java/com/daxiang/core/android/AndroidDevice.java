package com.daxiang.core.android;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.daxiang.App;
import com.daxiang.api.MasterApi;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.android.scrcpy.Scrcpy;
import com.daxiang.core.android.stf.AdbKit;
import com.daxiang.core.android.stf.Minicap;
import com.daxiang.core.android.stf.Minitouch;
import com.daxiang.core.appium.AndroidDriverBuilder;
import com.daxiang.core.appium.AndroidNativePageSourceHandler;
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
import org.openqa.selenium.OutputType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Data
public class AndroidDevice extends MobileDevice {

    public final static String TMP_FOLDER = "/data/local/tmp";

    private IDevice iDevice;

    private Scrcpy scrcpy;
    private Minicap minicap;
    private Minitouch minitouch;
    private AdbKit adbKit;

    private boolean canUseAppiumRecordVideo = true;

    public AndroidDevice(Device device, IDevice iDevice, AppiumServer appiumServer) {
        super(device, appiumServer);
        this.iDevice = iDevice;
    }

    public void setIDevice(IDevice iDevice) {
        // 手机重新插拔后，IDevice需要更新
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
        Integer sdkVersion = Integer.parseInt(AndroidUtil.getSdkVersion(iDevice)); // 21 android5.0
        return sdkVersion >= 21;
    }

    @Override
    public AppiumDriver initAppiumDriver() {
        return new AndroidDriverBuilder().init(this);
    }

    @Override
    public File screenshot() {
        return getAppiumDriver().getScreenshotAs(OutputType.FILE);
    }

    @Override
    public void installApp(File appFile) throws InstallException {
        AndroidUtil.installApk(iDevice, appFile.getAbsolutePath());
    }

    @Override
    public String dump() throws IOException, DocumentException {
        return new AndroidNativePageSourceHandler(getAppiumDriver()).getPageSource();
    }

    @Override
    public void startRecordingScreen() {
        if (canUseAppiumRecordVideo) {
            try {
                AndroidStartScreenRecordingOptions androidOptions = new AndroidStartScreenRecordingOptions();
                // Since Appium 1.8.2 the time limit can be up to 1800 seconds (30 minutes).
                androidOptions.withTimeLimit(Duration.ofMinutes(30));
                androidOptions.withBitRate(2000000); // default 4000000
                ((AndroidDriver) getAppiumDriver()).startRecordingScreen(androidOptions);
                return;
            } catch (Exception e) {
                log.warn("[{}]无法使用appium录制视频，改用minicap录制视频", getId(), e);
                canUseAppiumRecordVideo = false;
            }
        }

        // todo 使用scrcpy录屏
    }

    @Override
    public File stopRecordingScreen() throws IOException {
        if (canUseAppiumRecordVideo) {
            File videoFile = new File(UUIDUtil.getUUID() + ".mp4");
            String base64Video = ((AndroidDriver) getAppiumDriver()).stopRecordingScreen();
            FileUtils.writeByteArrayToFile(videoFile, Base64.getDecoder().decode(base64Video), false);
            return videoFile;
        } else {
            // todo 使用scrcpy录屏
            return null;
        }
    }

    @Override
    public void installApp(String appDownloadUrl) throws Exception {
        File apk = downloadApp(appDownloadUrl);
        ScheduledExecutorService service = handleInstallBtnAsync();
        try {
            AndroidUtil.installApk(iDevice, apk.getAbsolutePath());
        } finally {
            FileUtils.deleteQuietly(apk);
            if (!service.isShutdown()) {
                service.shutdown();
            }
        }
    }

    /**
     * 处理安装app时弹窗
     */
    private ScheduledExecutorService handleInstallBtnAsync() {
        final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            try {
                getAppiumDriver().executeScript("mobile:acceptAlert");
            } catch (Exception ignore) {
            }
        }, 0, 1, TimeUnit.SECONDS);
        return service;
    }

    public synchronized Optional<String> getChromedriverFilePath() {
        Optional<String> chromedriverDownloadUrl = MasterApi.getInstance().getChromedriverDownloadUrl(getId());
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
            // 文件不存在 -> 下载
            try {
                log.info("[chromedriver][{}]download => {}", getId(), downloadUrl);
                FileUtils.writeByteArrayToFile(chromedriverFile, App.getBean(RestTemplate.class).getForObject(downloadUrl, byte[].class), false);
            } catch (IOException e) {
                log.error("write chromedriver file err", e);
                return Optional.empty();
            }
        } else {
            log.info("[chromedriver][{}]file exist => {}", getId(), chromedriverFile.getAbsolutePath());
        }

        return Optional.of(chromedriverFile.getAbsolutePath());
    }
}
