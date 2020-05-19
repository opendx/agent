package com.daxiang.core.mobile.ios;

import com.daxiang.App;
import com.daxiang.core.PortProvider;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.core.mobile.appium.AppiumServer;
import com.daxiang.core.mobile.appium.IosNativePageSourceHandler;
import com.daxiang.core.mobile.Mobile;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.util.StringUtils;

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
    public RemoteWebDriver newDriver() {
        return new IOSDriver(getDeviceServer().getUrl(), this.caps);
    }

    @Override
    protected Capabilities newCaps(Capabilities capsToMerge) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true);
        capabilities.setCapability("waitForQuiescence", false);
        capabilities.setCapability("skipLogCapture", true);
        capabilities.setCapability("useJSONSource", true); // Get JSON source from WDA and parse into XML on Appium server. This can be much faster, especially on large devices.

        // https://github.com/appium/appium-xcuitest-driver/blob/master/docs/real-device-config.md
        String xcodeOrgId = App.getProperty("xcodeOrgId");
        if (!StringUtils.isEmpty(xcodeOrgId)) {
            capabilities.setCapability("xcodeOrgId", xcodeOrgId);
        }
        String xcodeSigningId = App.getProperty("xcodeSigningId");
        if (!StringUtils.isEmpty(xcodeSigningId)) {
            capabilities.setCapability("xcodeSigningId", xcodeSigningId);
        }
        String updatedWDABundleId = App.getProperty("updatedWDABundleId");
        if (!StringUtils.isEmpty(updatedWDABundleId)) {
            capabilities.setCapability("updatedWDABundleId", updatedWDABundleId);
        }

        // **** 以上capabilities可被传入的caps覆盖 ****

        capabilities.merge(capsToMerge);

        // **** 以下capabilities具有更高优先级，将覆盖传入的caps ****

        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
        capabilities.setCapability(IOSMobileCapabilityType.WDA_LOCAL_PORT, PortProvider.getWdaLocalAvailablePort());

        capabilities.setCapability("mjpegServerPort", PortProvider.getWdaMjpegServerAvailablePort());
        capabilities.setCapability("webkitDebugProxyPort", PortProvider.getWebkitDebugProxyAvalilablePort());

        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, mobile.getName());
        capabilities.setCapability(MobileCapabilityType.UDID, getId());
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, mobile.getSystemVersion());
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, NEW_COMMAND_TIMEOUT);

        return capabilities;
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
