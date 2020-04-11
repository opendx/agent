package com.daxiang.core.ios;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.core.appium.IosNativePageSourceHandler;
import com.daxiang.model.Device;
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
     * iproxy localPort remotePort deviceId
     */
    private static final String IPROXY_CMD = "iproxy %d %d %s";

    private ExecuteWatchdog iproxyMjpegServerWatchdog;

    public IosDevice(Device device, AppiumServer appiumServer) {
        super(device, appiumServer);
    }

    @Override
    public void installApp(File appFile) throws IOException {
        try {
            IosUtil.installIpa(appFile.getAbsolutePath(), getId());
        } finally {
            FileUtils.deleteQuietly(appFile);
        }
    }

    @Override
    public void uninstallApp(String app) {
        IosUtil.uninstallApp(getAppiumDriver(), app);
    }

    @Override
    public String dump() throws IOException, DocumentException {
        return new IosNativePageSourceHandler(getAppiumDriver()).getPageSource();
    }

    @Override
    public boolean acceptAlert() {
        try {
            getAppiumDriver().switchTo().alert().accept();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean dismissAlert() {
        try {
            getAppiumDriver().switchTo().alert().dismiss();
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
        ((IOSDriver) getAppiumDriver()).startRecordingScreen(iosOptions);
    }

    @Override
    public File stopRecordingScreen() throws IOException {
        File videoFile = new File(UUIDUtil.getUUID() + ".mp4");
        String base64Video = ((IOSDriver) getAppiumDriver()).stopRecordingScreen();
        FileUtils.writeByteArrayToFile(videoFile, Base64.getDecoder().decode(base64Video), false);
        return videoFile;
    }

    public int getMjpegServerPort() {
        Object mjpegServerPort = getAppiumDriver().getCapabilities().asMap().get("mjpegServerPort");
        return (int) ((long) mjpegServerPort);
    }

    public void startMjpegServerIproxy() throws IOException {
        int mjpegServerPort = getMjpegServerPort();
        String cmd = String.format(IPROXY_CMD, mjpegServerPort, mjpegServerPort, getId());
        log.info("[ios][{}]mjpegServer: {}", getId(), cmd);
        iproxyMjpegServerWatchdog = Terminal.executeAsyncAndGetWatchdog(cmd);
    }

    public void stopMjpegServerIproxy() {
        if (iproxyMjpegServerWatchdog != null) {
            log.info("[ios][{}]mjpegServer iproxy stop", getId());
            iproxyMjpegServerWatchdog.destroyProcess();
        }
    }
}
