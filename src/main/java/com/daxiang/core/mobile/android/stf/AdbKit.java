package com.daxiang.core.mobile.android.stf;

import com.daxiang.core.PortProvider;
import com.daxiang.utils.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.*;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AdbKit {

    private static final String LOCAL_ADBKIT = "vendor/adbkit/bin/adbkit";

    private ShutdownHookProcessDestroyer adbKitProcessDestroyer;
    private String mobileId;

    public AdbKit(String mobileId) {
        this.mobileId = mobileId;
    }

    /**
     * 开启远程调试功能
     *
     * @throws IOException
     */
    public int start() throws IOException {
        stop();

        int localPort = PortProvider.getAdbKitAvailablePort();
        // https://github.com/openstf/adbkit
        String cmd = String.format("node %s usb-device-to-tcp -p %d %s", LOCAL_ADBKIT, localPort, mobileId);

        log.info("[{}]开启远程调试功能: {}", mobileId, cmd);
        adbKitProcessDestroyer = Terminal.executeAsync(cmd);

        return localPort;
    }

    /**
     * 关闭远程调试功能
     */
    public void stop() {
        if (adbKitProcessDestroyer != null) {
            log.info("[{}]关闭adbkit", mobileId);
            adbKitProcessDestroyer.run();
        }
    }
}
