package com.daxiang.core.mobile.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.daxiang.utils.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class ADB {

    public static void addDeviceChangeListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener) {
        AndroidDebugBridge.init(false);
        AndroidDebugBridge.createBridge(getPath(), false);
        AndroidDebugBridge.addDeviceChangeListener(deviceChangeListener);
    }

    public static void killServer() throws IOException {
        Terminal.execute("adb kill-server");
    }

    public static void startServer() throws IOException {
        Terminal.execute("adb start-server");
    }

    private static String getPath() {
        String androidHome = System.getenv("ANDROID_HOME");
        log.info("ANDROID_HOME: {}", androidHome);

        if (StringUtils.isEmpty(androidHome)) {
            throw new IllegalStateException("环境变量缺少ANDROID_HOME");
        }

        String adbPrefixPath = androidHome + File.separator + "platform-tools" + File.separator;
        String adbPath = Terminal.IS_WINDOWS ? adbPrefixPath + "adb.exe" : adbPrefixPath + "adb";
        log.info("adb路径: {}", adbPath);

        if (!Files.exists(Paths.get(adbPath))) {
            throw new IllegalStateException(adbPath + "文件不存在");
        }

        return adbPath;
    }
}