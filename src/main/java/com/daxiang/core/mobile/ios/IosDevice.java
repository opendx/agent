package com.daxiang.core.mobile.ios;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.mobile.appium.AppiumServer;
import com.daxiang.core.mobile.appium.IosNativePageSourceHandler;
import com.daxiang.model.Mobile;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ExecuteWatchdog;
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
public class IosDevice extends MobileDevice {

    /**
     * iproxy localPort remotePort mobileId
     */
    private static final String IPROXY_CMD = "iproxy %d %d %s";

    private ExecuteWatchdog iproxyMjpegServerWatchdog;

    public IosDevice(Mobile mobile, AppiumServer appiumServer) {
        super(mobile, appiumServer);
    }

    @Override
    public void uninstallApp(String app) {
        IosUtil.uninstallApp(appiumDriver, app);
    }

    @Override
    public String dumpNativePage() throws IOException, DocumentException {
        return new IosNativePageSourceHandler(appiumDriver).getPageSource();
    }

    @Override
    public boolean acceptAlert() {
        try {
            appiumDriver.switchTo().alert().accept();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean dismissAlert() {
        try {
            appiumDriver.switchTo().alert().dismiss();
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
        ((IOSDriver) appiumDriver).startRecordingScreen(iosOptions);
    }

    @Override
    public File stopRecordingScreen() throws IOException {
        File videoFile = new File(UUIDUtil.getUUID() + ".mp4");
        String base64Video = ((IOSDriver) appiumDriver).stopRecordingScreen();
        FileUtils.writeByteArrayToFile(videoFile, Base64.getDecoder().decode(base64Video), false);
        return videoFile;
    }

    public int getMjpegServerPort() {
        Object mjpegServerPort = appiumDriver.getCapabilities().asMap().get("mjpegServerPort");
        return (int) ((long) mjpegServerPort);
    }

    public int startMjpegServerIproxy() throws IOException {
        int mjpegServerPort = getMjpegServerPort();
        String cmd = String.format(IPROXY_CMD, mjpegServerPort, mjpegServerPort, getId());
        log.info("[ios][{}]mjpegServer: {}", getId(), cmd);
        iproxyMjpegServerWatchdog = Terminal.executeAsyncAndGetWatchdog(cmd);
        return mjpegServerPort;
    }

    public void stopMjpegServerIproxy() {
        if (iproxyMjpegServerWatchdog != null) {
            log.info("[ios][{}]mjpegServer iproxy stop", getId());
            iproxyMjpegServerWatchdog.destroyProcess();
        }
    }
}
