package com.daxiang.core.ios;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.core.appium.IosDriverBuilder;
import com.daxiang.core.appium.IosPageSourceHandler;
import com.daxiang.model.Device;
import com.daxiang.utils.Terminal;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class IosDevice extends MobileDevice {

    private ExecuteWatchdog mjpegServerIproxyWatchdog;

    public IosDevice(Device device, AppiumServer appiumServer) {
        super(device, appiumServer);
    }

    @Override
    public AppiumDriver newAppiumDriver() {
        return new IosDriverBuilder().build(this);
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

    public int getMjpegServerPort() {
        Object mjpegServerPort = getAppiumDriver().getCapabilities().asMap().get("mjpegServerPort");
        return (int) ((long) mjpegServerPort);
    }

    public void startMjpegServerIproxy() throws IOException {
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(new LogOutputStream() {
            @Override
            protected void processLine(String line, int i) {
                log.info("[ios][{}]iproxy -> {}", getId(), line);
            }
        });
        int mjpegServerPort = getMjpegServerPort();
        // iproxy localPort remotePort deviceId
        mjpegServerIproxyWatchdog = Terminal
                .executeAsyncAndGetWatchdog(pumpStreamHandler, "iproxy", String.valueOf(mjpegServerPort), String.valueOf(mjpegServerPort), getId());
        log.info("[ios][{}]mjpegServer: iproxy {} {} {}", getId(), mjpegServerPort, mjpegServerPort, getId());
    }

    public void stopMjpegServerIproxy() {
        if (mjpegServerIproxyWatchdog != null) {
            mjpegServerIproxyWatchdog.destroyProcess();
            log.info("[ios][{}]mjpegServer iproxy stop", getId());
        }
    }
}
