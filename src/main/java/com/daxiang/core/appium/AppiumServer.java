package com.daxiang.core.appium;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AppiumServer {

    private AppiumDriverLocalService service;

    public void start() {
        AppiumServiceBuilder builder = new AppiumServiceBuilder();
        builder.usingAnyFreePort().withArgument(GeneralServerFlag.SESSION_OVERRIDE);
        service = AppiumDriverLocalService.buildService(builder);
//        service.enableDefaultSlf4jLoggingOfOutputData(); // 输出日志到slf4j
        service.start();
    }

    public URL getUrl() {
        if (service != null && service.isRunning()) {
            return service.getUrl();
        }
        return null;
    }

    public void stop() {
        if (service != null && service.isRunning()) {
            service.stop();
        }
    }
}
