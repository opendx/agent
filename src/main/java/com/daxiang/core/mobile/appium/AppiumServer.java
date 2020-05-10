package com.daxiang.core.mobile.appium;

import com.daxiang.App;
import com.daxiang.core.DeviceServer;
import com.daxiang.core.PortProvider;
import com.daxiang.utils.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ExecuteWatchdog;
import org.openqa.selenium.net.UrlChecker;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AppiumServer extends DeviceServer {

    private static final String APPIUM_JS = App.getProperty("appiumJs");
    private static String version;

    public static String getVersion() throws IOException {
        if (version == null) {
            if (StringUtils.isEmpty(APPIUM_JS)) {
                version = Terminal.execute("appium -v");
            } else {
                version = Terminal.execute("node " + APPIUM_JS + " -v");
            }
        }
        return version;
    }

    private ExecuteWatchdog watchdog;

    @Override
    public synchronized void start() {
        if (isRunning) {
            return;
        }

        int port = PortProvider.getAppiumServerAvailablePort();

        String startCmd;
        if (StringUtils.isEmpty(APPIUM_JS)) {
            startCmd = String.format("appium -p %d --session-override", port);
        } else {
            startCmd = String.format("node %s -p %d --session-override", APPIUM_JS, port);
        }

        try {
            watchdog = Terminal.executeAsyncAndGetWatchdog(startCmd);
            String url = "http://localhost:" + port + "/wd/hub";
            this.url = new URL(url);
            new UrlChecker().waitUntilAvailable(60, TimeUnit.SECONDS, new URL(url + "/status"));
            isRunning = true;
        } catch (Exception e) {
            throw new RuntimeException("启动appium server失败", e);
        }

    }

    @Override
    public synchronized void stop() {
        if (isRunning) {
            watchdog.destroyProcess();
            isRunning = false;
        }
    }
}
