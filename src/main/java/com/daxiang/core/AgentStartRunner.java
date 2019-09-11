package com.daxiang.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.android.ADB;
import com.daxiang.core.android.AndroidDeviceChangeListener;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.core.ios.IosDeviceChangeListener;
import com.daxiang.core.ios.IosDeviceMonitor;
import com.daxiang.utils.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class AgentStartRunner implements ApplicationRunner {

    /**
     * todo 补充文档
     * 防止切换webview报错
     * 1. chrome://inspect 查看手机webview chrome版本
     * 2. https://chromedriver.storage.googleapis.com/2.41/notes.txt 找到手机匹配的chromedriver版本
     * 3. https://npm.taobao.org/mirrors/chromedriver 下载相应版本的chromedriver
     * 文件编码utf-8
     * eg. {"deviceIdA": "/path/chromedriver", "deviceIdB": "c:/path/chromedriver.exe"}
     */
    private static final String DEVICE_CHROMEDRIVER_JSON_FILE = "device_chromedriver.json";

    /**
     * deviceId : chromeDriverFilePath
     */
    private static JSONObject deviceIdChromeDriverFilePath;

    @Autowired
    private AndroidDeviceChangeListener androidDeviceChangeListener;
    @Autowired
    private IosDeviceChangeListener iosDeviceChangeListener;

    @Value("${android}")
    private boolean needAndroid;
    @Value("${ios}")
    private boolean needIos;

    @Override
    public void run(ApplicationArguments args) throws IOException, InterruptedException {
        if (needAndroid) {
            ADB.killServer();
            Thread.sleep(1000);
            ADB.startServer();
            ADB.addDeviceChangeListener(androidDeviceChangeListener);
            log.info("[android]开始监听设备连接/断开");
        }

        if (needIos) {
            IosDeviceMonitor iosDeviceMonitor = IosDeviceMonitor.getInstance();
            iosDeviceMonitor.start(iosDeviceChangeListener);
            log.info("[ios]开始监听设备连接/断开");
        }

        File deviceChromeDriverJsonFile = new File(DEVICE_CHROMEDRIVER_JSON_FILE);
        if (deviceChromeDriverJsonFile.exists()) {
            log.info("检测到{}", DEVICE_CHROMEDRIVER_JSON_FILE);
            String deviceChromeDriverJsonFileContent = FileUtils.readFileToString(deviceChromeDriverJsonFile, Charset.forName("UTF-8"));
            log.info("{} => {}", DEVICE_CHROMEDRIVER_JSON_FILE, deviceChromeDriverJsonFileContent);
            deviceIdChromeDriverFilePath = JSON.parseObject(deviceChromeDriverJsonFileContent);
        }

        // appium版本
        String appiumVersion = AppiumServer.getVersion();
        System.setProperty("appiumVersion", appiumVersion);

        // 是否配置了aapt
        String aaptVersion = Terminal.execute("aapt v");
        if (!StringUtils.isEmpty(aaptVersion) && aaptVersion.startsWith("Android")) {
            System.setProperty("aapt", "true");
        } else {
            System.setProperty("aapt", "false");
        }
    }

    public static String getChromeDriverFilePath(String deviceId) {
        return deviceIdChromeDriverFilePath == null ? null : deviceIdChromeDriverFilePath.getString(deviceId);
    }
}
