package com.daxiang.core.ios;

import com.daxiang.core.MobileDevice;
import com.daxiang.model.Device;
import com.daxiang.utils.ShellExecutor;
import io.appium.java_client.touch.offset.PointOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.IOException;

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

    public IosDevice(Device device) {
        super(device);
    }

    public int getMjpegServerPort() {
        Object mjpegServerPort = getAppiumDriver().getCapabilities().asMap().get("mjpegServerPort");
        return (int) ((long) mjpegServerPort);
    }

    public void startMjpegServerIproxy() throws IOException {
        int mjpegServerPort = getMjpegServerPort();
        String cmd = String.format(IPROXY, mjpegServerPort, mjpegServerPort, getId());
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(new LogOutputStream() {
            @Override
            protected void processLine(String s, int i) {
                log.info("[ios][{}]iproxy -> {}", getId(), s);
            }
        });
        mjpegServerIproxyWatchdog = ShellExecutor.excuteAsyncAndGetWatchdog(cmd, pumpStreamHandler);
        log.info("[ios][{}]mjpegServer: {}", getId(), cmd);
    }

    public void stopMjpegServerIproxy() {
        if (mjpegServerIproxyWatchdog != null) {
            mjpegServerIproxyWatchdog.destroyProcess();
            log.info("[ios][{}]mjpegServer iproxy stop", getId());
        }
    }

    public PointOption getPointOption(float percentOfX, float percentOfY) {
        int screenWidth = getDevice().getScreenWidth();
        int screenHeight = getDevice().getScreenHeight();
        return PointOption.point((int) (percentOfX * screenWidth), (int) (percentOfY * screenHeight));
    }
}
