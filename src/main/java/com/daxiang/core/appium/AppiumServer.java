package com.daxiang.core.appium;

import com.daxiang.App;
import com.daxiang.core.PortProvider;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URL;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AppiumServer {

    private AppiumDriverLocalService service;

    public void start() {
        AppiumServiceBuilder builder = new AppiumServiceBuilder();
        builder.usingPort(PortProvider.getAppiumServerAvailablePort()).withArgument(GeneralServerFlag.SESSION_OVERRIDE);
        String appiumJs = App.getProperty("appiumJs");
        if (!StringUtils.isEmpty(appiumJs)) {
            log.info("[appium-server]appiumJs: {}", appiumJs);
            builder.withAppiumJS(new File(appiumJs));
        }
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
