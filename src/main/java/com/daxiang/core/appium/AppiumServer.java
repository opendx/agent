package com.daxiang.core.appium;

import com.daxiang.App;
import com.daxiang.core.PortProvider;
import com.daxiang.utils.Terminal;
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

    private static final String APPIUM_JS = App.getProperty("appiumJs");
    private static String version;

    public synchronized static String getVersion() {
        if (version == null) {
            try {
                if (StringUtils.isEmpty(APPIUM_JS)) {
                    version = Terminal.execute("appium -v");
                } else {
                    version = Terminal.execute("node " + APPIUM_JS + " -v");
                }
            } catch (Exception e) {
                log.error("获取appium版本失败", e);
                version = e.getMessage();
            }
        }
        return version;
    }

    private AppiumDriverLocalService service;

    public void start() {
        AppiumServiceBuilder builder = new AppiumServiceBuilder();
        builder.usingPort(PortProvider.getAppiumServerAvailablePort()).withArgument(GeneralServerFlag.SESSION_OVERRIDE);

        if (!StringUtils.isEmpty(APPIUM_JS)) {
            log.info("[appium-server]appiumJs: {}", APPIUM_JS);
            builder.withAppiumJS(new File(APPIUM_JS));
        }
        service = AppiumDriverLocalService.buildService(builder);
//        service.enableDefaultSlf4jLoggingOfOutputData(); // 输出日志到slf4j
        service.start();
    }

    public URL getUrl() {
        if (service == null) {
            throw new RuntimeException("appium服务未启动");
        }
        // 从日志里看到很多人服务已经成功运行，但是这个方法检测不到isRunning，先注掉
//        if (!service.isRunning()) {
//            throw new RuntimeException("appium服务未运行");
//        }
        URL url = service.getUrl();
        if (url == null) {
            throw new RuntimeException("appium服务url为空");
        }
        return url;
    }

    public void stop() {
//        if (service != null && service.isRunning()) {
//            service.stop();
//        }
        // 很多人服务已经在运行但是service.isRunning()检测不到已运行，所有先这样处理
        if (service != null) {
            service.stop();
        }
    }
}
