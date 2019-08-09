package com.daxiang.core.android;

import com.android.ddmlib.*;
import com.daxiang.utils.ShellExecutor;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AndroidUtil {

    private static final String CPU_INFO_SHELL = "cat /proc/cpuinfo |grep Hardware";
    private static final String MEM_SIZE_SHELL = "cat /proc/meminfo |grep MemTotal";

    public static final Map<String, String> ANDROID_VERSION = new HashMap();

    static {
        // https://source.android.com/setup/start/build-numbers
        // uiautomator需要 >= 4.2
        ANDROID_VERSION.put("17", "4.2");
        ANDROID_VERSION.put("18", "4.3");
        ANDROID_VERSION.put("19", "4.4");
        ANDROID_VERSION.put("20", "4.4W");
        ANDROID_VERSION.put("21", "5.0");
        ANDROID_VERSION.put("22", "5.1");
        ANDROID_VERSION.put("23", "6.0");
        ANDROID_VERSION.put("24", "7.0");
        ANDROID_VERSION.put("25", "7.1");
        ANDROID_VERSION.put("26", "8.0");
        ANDROID_VERSION.put("27", "8.1");
        ANDROID_VERSION.put("28", "9");
        ANDROID_VERSION.put("29", "10");
    }

    /**
     * 获取CPU信息
     *
     * @return
     */
    public static String getCpuInfo(IDevice iDevice) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        String output = executeShellCommand(iDevice, CPU_INFO_SHELL);
        if (StringUtils.isEmpty(output)) {
            throw new RuntimeException("获取CPU信息失败");
        }
        return output.split(":")[1].trim();
    }

    /**
     * 获取内存信息
     *
     * @return
     */
    public static String getMemSize(IDevice iDevice) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        String output = executeShellCommand(iDevice, MEM_SIZE_SHELL);
        if (StringUtils.isEmpty(output)) {
            throw new RuntimeException("获取内存信息失败");
        }
        String memKB = (output.replaceAll(" ", "")).replaceAll("\n", "").replaceAll("\r", "").split(":")[1];
        memKB = memKB.substring(0, memKB.length() - 2);
        // 向上取整
        double memGB = Math.ceil(Long.parseLong(memKB) / (1024.0 * 1024));
        return memGB + " GB";
    }

    /**
     * 设备名
     *
     * @return
     */
    public static String getDeviceName(IDevice iDevice) {
        return "[" + iDevice.getProperty("ro.product.brand") + "] " + iDevice.getProperty("ro.product.model");
    }

    /**
     * 安卓版本
     *
     * @return
     */
    public static String getAndroidVersion(IDevice iDevice) {
        return ANDROID_VERSION.get(getSdkVersion(iDevice));
    }

    /**
     * 通过minicap截图
     *
     * @param iDevice
     * @param resolution 手机分辨率 eg. 1080x1920
     */
    public static File screenshotByMinicap(IDevice iDevice, String resolution) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, SyncException {
        String localScreenshotFilePath = UUIDUtil.getUUID() + ".jpg";
        String remoteScreenshotFilePath = AndroidDevice.TMP_FOLDER + "minicap.jpg";

        String screenshotCmd = String.format("LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/0 -s >%s", resolution, resolution, remoteScreenshotFilePath);
        String minicapOutput = executeShellCommand(iDevice, screenshotCmd);

        if (StringUtils.isEmpty(minicapOutput) || !minicapOutput.contains("bytes for JPG encoder")) {
            throw new RuntimeException("minicap截图失败, cmd: " + screenshotCmd + ", minicapOutput: " + minicapOutput);
        }
        // pull到本地
        iDevice.pullFile(remoteScreenshotFilePath, localScreenshotFilePath);
        return new File(localScreenshotFilePath);
    }

    /**
     * 获取CPU架构
     *
     * @return
     */
    public static String getCpuAbi(IDevice iDevice) {
        return iDevice.getAbis().stream().findFirst().orElseThrow(() -> new RuntimeException("获取CPU架构失败"));
    }

    /**
     * 获取手机sdk版本
     *
     * @return
     */
    public static String getSdkVersion(IDevice iDevice) {
        return iDevice.getProperty("ro.build.version.sdk");
    }

    /**
     * 等待设备上线
     */
    public static void waitForDeviceOnline(IDevice iDevice, long maxWaitTimeInSeconds) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime > maxWaitTimeInSeconds * 1000) {
                throw new RuntimeException("[" + iDevice.getSerialNumber() + "]设备未上线");
            }
            if (iDevice.isOnline()) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    /**
     * 安装APK
     *
     * @param iDevice
     * @param apkPath
     * @throws InstallException
     */
    public static void installApk(IDevice iDevice, String apkPath) throws InstallException {
        iDevice.installPackage(apkPath, true);
    }

    public static void uninstallApk(IDevice iDevice, String packageName) throws InstallException {
        iDevice.uninstallPackage(packageName);
    }

    /**
     * 执行命令
     *
     * @param cmd
     * @return
     */
    public static String executeShellCommand(IDevice iDevice, String cmd) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        CollectingOutputReceiver collectingOutputReceiver = new CollectingOutputReceiver();
        iDevice.executeShellCommand(cmd, collectingOutputReceiver);
        return collectingOutputReceiver.getOutput();
    }

    /**
     * aapt dump badging
     */
    public static String aaptDumpBadging(String apkPath) throws IOException {
        String cmd = "aapt dump badging " + apkPath;
        return ShellExecutor.execute(cmd);
    }

    /**
     * 清除apk数据
     */
    public static void clearApkData(IDevice iDevice, String packageName) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        iDevice.executeShellCommand("pm clear " + packageName, new NullOutputReceiver());
    }

    /**
     * 重启apk
     */
    public static void restartApk(IDevice iDevice, String packageName, String launchActivity) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        iDevice.executeShellCommand("am start -S -n " + packageName + "/" + launchActivity, new NullOutputReceiver());
    }
}
