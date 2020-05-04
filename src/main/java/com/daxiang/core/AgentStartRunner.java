package com.daxiang.core;

import com.daxiang.core.android.ADB;
import com.daxiang.core.android.AndroidDeviceChangeListener;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.core.ios.IosDeviceChangeListener;
import com.daxiang.core.ios.IosDeviceMonitor;
import com.daxiang.core.pcweb.BrowserInitializer;
import com.daxiang.utils.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class AgentStartRunner implements ApplicationRunner {

    @Autowired
    private AndroidDeviceChangeListener androidDeviceChangeListener;
    @Autowired
    private IosDeviceChangeListener iosDeviceChangeListener;
    @Autowired
    private BrowserInitializer browserInitializer;

    @Value("${android}")
    private boolean enableAndroid;
    @Value("${ios}")
    private boolean enableIos;
    @Value("${pc-web}")
    private boolean enablePcWeb;

    @Override
    public void run(ApplicationArguments args) throws IOException, InterruptedException {
        // 移动端
        if (enableAndroid || enableIos) {
            String appiumVersion = AppiumServer.getVersion();
            checkAppiumVersion(appiumVersion);
            System.setProperty("appium.version", appiumVersion);

            if (enableAndroid) {
                ADB.killServer();
                Thread.sleep(1000);
                ADB.startServer();
                ADB.addDeviceChangeListener(androidDeviceChangeListener);
                log.info("[android]开始监听设备连接/断开");
            } else {
                log.info("[android]未开启Android功能");
            }

            if (enableIos) {
                IosDeviceMonitor iosDeviceMonitor = IosDeviceMonitor.getInstance();
                iosDeviceMonitor.start(iosDeviceChangeListener);
                log.info("[ios]开始监听设备连接/断开");
            } else {
                log.info("[ios]未开启ios功能");
            }
        }

        // pc端
        if (enablePcWeb) {
            browserInitializer.init();
        } else {
            log.info("[web]未开启pc web功能");
        }

        // 是否配置了aapt
        String aaptVersion = Terminal.execute("aapt v");
        if (!StringUtils.isEmpty(aaptVersion) && aaptVersion.startsWith("Android")) {
            System.setProperty("aapt", "true");
        } else {
            System.setProperty("aapt", "false");
        }

        // ffmpeg
        Terminal.execute("ffmpeg -version");
    }

    private void checkAppiumVersion(String appiumVersion) {
        if (StringUtils.isEmpty(appiumVersion) || !appiumVersion.matches("\\d+.\\d+.\\d+")) {
            throw new IllegalArgumentException("appium版本错误: " + appiumVersion);
        }

        String[] appiumVersionArr = appiumVersion.split("\\.");
        int first = Integer.parseInt(appiumVersionArr[0]);
        int middle = Integer.parseInt(appiumVersionArr[1]);

        if (first < 1 || (first == 1 && middle < 16)) {
            throw new IllegalArgumentException("appium版本必须>=1.16.0");
        }
    }

}
