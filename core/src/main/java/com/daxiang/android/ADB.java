package com.daxiang.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.daxiang.actions.utils.ShellExecutor;
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
    private static AndroidDebugBridge adb;

    /**
     * 初始化ADB
     */
    public static synchronized void init() {
        if (adb == null) {
            log.info("开始初始化adb");
            AndroidDebugBridge.init(false);
            adb = AndroidDebugBridge.createBridge(getPath(), false);

            log.info("等待adb连接");
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start <= 30000) {
                if (adb.isConnected()) {
                    log.info("adb已连接");
                    log.info("adb初始化成功");
                    return;
                }
            }
            throw new RuntimeException("adb初始化失败");
        }
    }

    /**
     * 添加Android设备监听器
     *
     * @param deviceChangeListener
     */
    public static void addDeviceChangeListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener) {
        AndroidDebugBridge.addDeviceChangeListener(deviceChangeListener);
        log.info("AndroidDeviceChangeListener添加完成");
    }

    /**
     * 杀掉adb服务
     */
    public static void killServer() throws IOException {
        log.info("开始kill adb-server");
        ShellExecutor.exec("adb kill-server");
        log.info("kill adb-server成功");
    }


    /**
     * 获取adb路径
     *
     * @return
     */
    private static String getPath() {
        String androidHome = System.getenv("ANDROID_HOME");
        log.info("环境变量ANDROID_HOME: {}", androidHome);

        if (StringUtils.isEmpty(androidHome)) {
            throw new RuntimeException("未获取到ANDROID_HOME，请配置ANDOIRD_HOME环境变量");
        }

        String adbPath = androidHome + File.separator + ADB_PLATFORM_TOOLS + File.separator + "adb";
        log.info("adb路径: {}", adbPath);
        return adbPath;
    }
}
