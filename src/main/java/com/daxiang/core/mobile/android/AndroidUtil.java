package com.daxiang.core.mobile.android;

import com.android.ddmlib.*;
import com.daxiang.utils.Terminal;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AndroidUtil {

    // https://source.android.com/setup/start/build-numbers
    public static final Map<Integer, String> ANDROID_VERSION_MAP = new ImmutableMap.Builder<Integer, String>()
            .put(17, "4.2") // uiautomator需要 >= 4.2
            .put(18, "4.3")
            .put(19, "4.4")
            .put(20, "4.4W")
            .put(21, "5.0")
            .put(22, "5.1")
            .put(23, "6.0")
            .put(24, "7.0")
            .put(25, "7.1")
            .put(26, "8.0")
            .put(27, "8.1")
            .put(28, "9")
            .put(29, "10")
            .put(30, "11")
            .build();

    public static String getCpuInfo(IDevice iDevice) throws IDeviceExecuteShellCommandException {
        String cpuInfo = executeShellCommand(iDevice, "cat /proc/cpuinfo |grep Hardware"); // Hardware	: Qualcomm Technologies, Inc MSM8909
        if (StringUtils.isEmpty(cpuInfo) || !cpuInfo.startsWith("Hardware")) {
            return null;
        }

        return cpuInfo.split(":")[1].trim();
    }

    public static String getMemSize(IDevice iDevice) throws IDeviceExecuteShellCommandException {
        String memInfo = executeShellCommand(iDevice, "cat /proc/meminfo |grep MemTotal"); // MemTotal:        1959700 kB
        if (StringUtils.isEmpty(memInfo) || !memInfo.startsWith("MemTotal")) {
            return null;
        }

        String memKB = Pattern.compile("[^0-9]").matcher(memInfo).replaceAll("").trim();
        return Math.ceil(Long.parseLong(memKB) / (1024.0 * 1024)) + " GB";
    }

    public static String getDeviceName(IDevice iDevice) {
        String brand = iDevice.getProperty("ro.product.brand");
        String model = iDevice.getProperty("ro.product.model");
        return String.format("[%s] %s", brand, model);
    }

    public static String getAndroidVersion(Integer sdkVerison) {
        return ANDROID_VERSION_MAP.get(sdkVerison);
    }

    /**
     * 获取CPU架构
     *
     * @return
     */
    public static String getCpuAbi(IDevice iDevice) {
        return iDevice.getProperty("ro.product.cpu.abi");
    }

    public static int getSdkVersion(IDevice iDevice) {
        return Integer.parseInt(iDevice.getProperty("ro.build.version.sdk"));
    }

    /**
     * 等待Mobile上线
     */
    public static void waitForMobileOnline(IDevice iDevice, long maxWaitTimeInSeconds) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime > maxWaitTimeInSeconds * 1000) {
                throw new RuntimeException(String.format("[%s]Mobile未上线，超时时间: %d秒", iDevice.getSerialNumber(), maxWaitTimeInSeconds));
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
    public static String executeShellCommand(IDevice iDevice, String cmd) throws IDeviceExecuteShellCommandException {
        Assert.notNull(iDevice, "iDevice can not be null");
        Assert.hasText(cmd, "cmd can not be empty");
        String mobileId = iDevice.getSerialNumber();

        CollectingOutputReceiver collectingOutputReceiver = new CollectingOutputReceiver();
        try {
            log.info("[{}]execute: {}", mobileId, cmd);
            iDevice.executeShellCommand(cmd, collectingOutputReceiver);
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            throw new IDeviceExecuteShellCommandException(e);
        }

        String response = collectingOutputReceiver.getOutput();
        log.info("[{}]response: {}", mobileId, response);
        return response;
    }

    public static String aaptDumpBadging(String apkPath) throws IOException {
        return Terminal.execute("aapt dump badging " + apkPath);
    }

    public static void clearApkData(IDevice iDevice, String packageName) throws IDeviceExecuteShellCommandException {
        Assert.hasText(packageName, "packageName must has text");
        executeShellCommand(iDevice, "pm clear " + packageName);
    }

    public static void restartApk(IDevice iDevice, String packageName, String launchActivity) throws IDeviceExecuteShellCommandException {
        Assert.hasText(packageName, "packageName must has text");
        Assert.hasText(launchActivity, "launchActivity must has text");

        executeShellCommand(iDevice, "am start -S -n " + packageName + "/" + launchActivity);
    }

    /**
     * 获取屏幕分辨率
     *
     * @return eg.720x1280
     */
    public static String getResolution(IDevice iDevice) throws IDeviceExecuteShellCommandException {
        String wmSize = executeShellCommand(iDevice, "wm size");

        Pattern pattern = Pattern.compile("Physical size: (\\d+x\\d+)");
        Matcher matcher = pattern.matcher(wmSize);
        while (matcher.find()) {
            return matcher.group(1);
        }

        throw new RuntimeException(String.format("[%s]cannot find physical size, wm size: %s", iDevice.getSerialNumber(), wmSize));
    }

    public static List<String> getImeList(IDevice iDevice) throws IDeviceExecuteShellCommandException {
        String imeListString = executeShellCommand(iDevice, "ime list -s");
        if (StringUtils.isEmpty(imeListString)) {
            return new ArrayList<>();
        }

        return Arrays.asList(imeListString.split("\\r?\\n"));
    }

    public static void setIme(IDevice iDevice, String ime) throws IDeviceExecuteShellCommandException {
        Assert.hasText(ime, "ime must has text");
        executeShellCommand(iDevice, "ime set " + ime);
    }

}
