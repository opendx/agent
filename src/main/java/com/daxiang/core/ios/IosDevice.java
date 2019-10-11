package com.daxiang.core.ios;

import com.daxiang.App;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.core.appium.IosDriverBuilder;
import com.daxiang.core.appium.IosPageSourceHandler;
import com.daxiang.model.Device;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class IosDevice extends MobileDevice {

    /**
     * iproxy localPort remotePort deviceId
     */
    private static final String IPROXY = "iproxy %d %d %s";
    private ExecuteWatchdog mjpegServerIproxyWatchdog;

    public IosDevice(Device device, AppiumServer appiumServer) {
        super(device, appiumServer);
    }

    @Override
    public AppiumDriver newAppiumDriver() {
        return new IosDriverBuilder().build(this, false);
    }

    @Override
    public AppiumDriver initAppiumDriver() {
        return new IosDriverBuilder().build(this, true);
    }

    @Override
    public File screenshot() throws Exception {
        return IosUtil.screenshotByIdeviceScreenshot(getId());
    }

    @Override
    public void installApp(File app) throws IOException {
        IosUtil.installIpa(app.getAbsolutePath(), getId());
    }

    @Override
    public String dump() throws IOException, DocumentException {
        return new IosPageSourceHandler(getAppiumDriver()).getPageSource();
    }

    @Override
    public void startRecordingScreen() {
        IOSStartScreenRecordingOptions iosOptions = new IOSStartScreenRecordingOptions();
        // The maximum value is 30 minutes.
        iosOptions.withTimeLimit(Duration.ofMinutes(30));
        iosOptions.withFps(10); // default 10
        iosOptions.withVideoQuality(IOSStartScreenRecordingOptions.VideoQuality.LOW);
        iosOptions.withVideoType("libx264");
        ((IOSDriver) getAppiumDriver()).startRecordingScreen(iosOptions);
    }

    @Override
    public File stopRecordingScreen() throws IOException {
        File videoFile = new File(UUIDUtil.getUUID() + ".mp4");
        String base64Video = ((IOSDriver) getAppiumDriver()).stopRecordingScreen();
        FileUtils.writeByteArrayToFile(videoFile, Base64.getDecoder().decode(base64Video), false);
        return videoFile;
    }

    @Override
    public void installApp(String appDownloadUrl) throws Exception {
        if (StringUtils.isEmpty(appDownloadUrl)) {
            throw new RuntimeException("appDownloadUrl cannot be empty");
        }

        // download ipa
        RestTemplate restTemplate = App.getBean(RestTemplate.class);
        byte[] ipaBytes = restTemplate.getForObject(appDownloadUrl, byte[].class);

        File ipa = new File(UUIDUtil.getUUID() + ".ipa");
        try {
            FileUtils.writeByteArrayToFile(ipa, ipaBytes, false);
            // install
            IosUtil.installIpa(ipa.getAbsolutePath(), getId());
        } finally {
            // delete ipa
            FileUtils.deleteQuietly(ipa);
        }
    }

    public int getMjpegServerPort() {
        Object mjpegServerPort = getAppiumDriver().getCapabilities().asMap().get("mjpegServerPort");
        return (int) ((long) mjpegServerPort);
    }

    public void startMjpegServerIproxy() throws IOException {
        int mjpegServerPort = getMjpegServerPort();
        String cmd = String.format(IPROXY, mjpegServerPort, mjpegServerPort, getId());
        log.info("[ios][{}]mjpegServer: {}", getId(), cmd);
        mjpegServerIproxyWatchdog = Terminal.executeAsyncAndGetWatchdog(cmd);
    }

    public void stopMjpegServerIproxy() {
        if (mjpegServerIproxyWatchdog != null) {
            log.info("[ios][{}]mjpegServer iproxy stop", getId());
            mjpegServerIproxyWatchdog.destroyProcess();
        }
    }
}
