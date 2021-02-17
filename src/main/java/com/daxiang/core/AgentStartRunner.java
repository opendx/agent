package com.daxiang.core;

import com.daxiang.core.action.BasicActionScanner;
import com.daxiang.core.classloader.AgentExtJarLoader;
import com.daxiang.core.mobile.android.ADB;
import com.daxiang.core.mobile.android.AndroidDeviceChangeListener;
import com.daxiang.core.mobile.appium.AppiumServer;
import com.daxiang.core.mobile.ios.IosDeviceChangeListener;
import com.daxiang.core.mobile.ios.IosDeviceMonitor;
import com.daxiang.core.pc.web.BrowserInitializer;
import com.daxiang.model.action.Action;
import com.daxiang.server.ServerClient;
import com.daxiang.utils.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class AgentStartRunner implements ApplicationRunner {

    @Autowired
    private ServerClient serverClient;

    @Value("${version}")
    private String version;
    @Value("${browserConfig}")
    private String browserConfig;
    @Value("${basicActionPackage}")
    private String basicActionPackage;

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
    public void run(ApplicationArguments args) throws Exception {
        System.setProperty("agent.version", version);

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
                log.info("开始监听AndroidDevice连接/断开");
            } else {
                log.info("未开启Android功能");
            }

            if (enableIos) {
                IosDeviceMonitor iosDeviceMonitor = IosDeviceMonitor.getInstance();
                iosDeviceMonitor.start(iosDeviceChangeListener);
                log.info("开始监听IosDevice连接/断开");
            } else {
                log.info("未开启iOS功能");
            }
        } else {
            log.info("未开启Android与iOS功能");
        }

        // pc端
        if (enablePcWeb) {
            browserInitializer.init(browserConfig);
        } else {
            log.info("未开启pc web功能");
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

        BasicActionScanner basicActionScanner = new BasicActionScanner();
        List<Action> basicActions = basicActionScanner.scanRecursive(basicActionPackage);
        log.info("scan: {}, basicActions: {}", basicActionPackage, basicActions);
        serverClient.resetBasicAction(basicActions);

        // 初始化extJars
        AgentExtJarLoader.getInstance().initExtJars();
    }

    private void checkAppiumVersion(String appiumVersion) {
        if (StringUtils.isEmpty(appiumVersion) || !appiumVersion.matches("\\d+.\\d+.\\d+")) {
            throw new IllegalArgumentException("非法的appium版本: " + appiumVersion);
        }

        String[] appiumVersionArr = appiumVersion.split("\\.");
        int first = Integer.parseInt(appiumVersionArr[0]);
        int middle = Integer.parseInt(appiumVersionArr[1]);

        if (first < 1 || (first == 1 && middle < 16)) {
            throw new IllegalArgumentException("appium版本不能低于1.16.0");
        }
    }

}
