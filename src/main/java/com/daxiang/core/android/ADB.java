package com.daxiang.core.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.daxiang.utils.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class ADB {

    private static final String ADB_PLATFORM_TOOLS = "platform-tools";

    /**
     * 连接ADB
     */
    public static void connect() {
        log.info("[adb]初始化adb");
        AndroidDebugBridge.init(false);
        log.info("[adb]创建adb");
        AndroidDebugBridge adb = AndroidDebugBridge.createBridge(getPath(), false);

        int timeoutInMs = 60 * 1000;
        long start = System.currentTimeMillis();
        while (true) {
            if (adb.hasInitialDeviceList()) {
                log.info("[adb]adb已连接");
                return;
            }
            if (System.currentTimeMillis() - start > timeoutInMs) {
                throw new RuntimeException("[adb]adb初始化失败，超时时间: " + timeoutInMs + "ms");
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    /**
     * 添加Android设备监听器
     *
     * @param deviceChangeListener
     */
    public static void addDeviceChangeListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener) {
        AndroidDebugBridge.addDeviceChangeListener(deviceChangeListener);
    }

    /**
     * 杀掉adb服务
     */
    public static void killServer() throws IOException {
        log.info("[adb]adb kill-server");
        Terminal.execute("adb kill-server");
        log.info("[adb]adb kill-server完成");
    }

    /**
     * 启动adb服务
     *
     * @throws IOException
     */
    public static void startServer() throws IOException {
        log.info("[adb]adb start-server");
        Terminal.execute("adb start-server");
        log.info("[adb]adb start-server完成");
    }

    /**
     * 获取adb路径
     *
     * @return
     */
    private static String getPath() {
        String androidHome = System.getenv("ANDROID_HOME");
        log.info("[adb]环境变量ANDROID_HOME: {}", androidHome);

        if (StringUtils.isEmpty(androidHome)) {
            throw new RuntimeException("未获取到ANDROID_HOME，请配置ANDOIRD_HOME环境变量");
        }

        String adbPath = androidHome + File.separator + ADB_PLATFORM_TOOLS + File.separator;
        if (Terminal.IS_WINDOWS) {
            adbPath = adbPath + "adb.exe";
        } else {
            adbPath = adbPath + "adb";
        }
        log.info("[adb]adb路径: {}", adbPath);
        return adbPath;
    }
}