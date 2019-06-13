package com.daxiang.android.stf;

import com.daxiang.actions.utils.ShellExecutor;
import com.daxiang.android.AndroidDevice;
import com.daxiang.android.PortProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.*;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AdbKit {

    /**
     * https://github.com/openstf/adbkit
     */
    private static final String START_ADBKIT_CMD = "node vendor/adbkit/bin/adbkit usb-device-to-tcp -p %d %s";

    private ExecuteWatchdog watchdog;
    private String deviceId;

    public AdbKit(AndroidDevice androidDevice) {
        this.deviceId = androidDevice.getId();
    }

    /**
     * 开启远程调试功能
     *
     * @throws IOException
     */
    public int start() throws IOException {
        stop();

        int localPort = PortProvider.getAdbKitAvailablePort();
        String cmd = String.format(START_ADBKIT_CMD, localPort, deviceId);
        log.info("[{}][adbkit]开启远程调试功能：{}", deviceId, cmd);
        watchdog = ShellExecutor.execReturnWatchdog(cmd);
        return localPort;
    }

    /**
     * 关闭远程调试功能
     */
    public void stop() {
        log.info("[{}][adbkit]关闭adbkit...", deviceId);
        if (watchdog != null) {
            watchdog.destroyProcess();
        }
        log.info("[{}][adbkit]关闭adbkit完成", deviceId);
    }

}
