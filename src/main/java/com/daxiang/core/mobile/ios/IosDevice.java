package com.daxiang.core.mobile.ios;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.core.mobile.appium.AppiumDriverFactory;
import com.daxiang.core.mobile.appium.AppiumServer;
import com.daxiang.core.mobile.appium.IosNativePageSourceHandler;
import com.daxiang.core.mobile.Mobile;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.remote.RemoteWebDriver;

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
     * iproxy localPort remotePort mobileId
     */
    private static final String IPROXY_CMD = "iproxy %d %d %s";

    private ExecuteWatchdog iproxyMjpegServerWatchdog;

    public IosDevice(Mobile mobile, AppiumServer appiumServer) {
        super(mobile, appiumServer);
        nativePageSourceHandler = new IosNativePageSourceHandler();
    }

    @Override
    public RemoteWebDriver newDriver(JSONObject caps) {
        return AppiumDriverFactory.createIosDriver(this, caps);
    }

    @Override
    public void uninstallApp(String app) {
        IosUtil.uninstallApp(driver, app);
    }

    @Override
    public boolean acceptAlert() {
        try {
            driver.switchTo().alert().accept();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean dismissAlert() {
        try {
            driver.switchTo().alert().dismiss();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void startRecordingScreen() {
        IOSStartScreenRecordingOptions iosOptions = new IOSStartScreenRecordingOptions();
        // The maximum value is 30 minutes.
        iosOptions.withTimeLimit(Duration.ofMinutes(30));
        iosOptions.withFps(10); // default 10
        iosOptions.withVideoQuality(IOSStartScreenRecordingOptions.VideoQuality.LOW);
        iosOptions.withVideoType("libx264");
        ((IOSDriver) driver).startRecordingScreen(iosOptions);
    }

    @Override
    public File stopRecordingScreen() throws IOException {
        File videoFile = new File(UUIDUtil.getUUID() + ".mp4");
        String base64Video = ((IOSDriver) driver).stopRecordingScreen();
        FileUtils.writeByteArrayToFile(videoFile, Base64.getDecoder().decode(base64Video), false);
        return videoFile;
    }

    public long getMjpegServerPort() {
        return (long) driver.getCapabilities().asMap().get("mjpegServerPort");
    }

    public long startMjpegServerIproxy() throws IOException {
        long mjpegServerPort = getMjpegServerPort();
        String cmd = String.format(IPROXY_CMD, mjpegServerPort, mjpegServerPort, getId());

        log.info("[{}]startMjpegServerIproxy: {}", getId(), cmd);
        iproxyMjpegServerWatchdog = Terminal.executeAsyncAndGetWatchdog(cmd);
        return mjpegServerPort;
    }

    public void stopMjpegServerIproxy() {
        if (iproxyMjpegServerWatchdog != null) {
            log.info("[{}]stopMjpegServerIproxy", getId());
            iproxyMjpegServerWatchdog.destroyProcess();
        }
    }

}
