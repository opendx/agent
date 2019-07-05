package com.daxiang.actions.utils;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class AndroidUtil {

    public static final String TMP_FOLDER = "/data/local/tmp/";

    /**
     * 执行adb shell命令
     */
    public static String excuteAdbShellCmd(String deviceId, String cmd) throws IOException {
        String adbShellCmd = "adb -s " + deviceId + " shell " + cmd;
        System.out.println("[" + deviceId + "]执行 -> " + adbShellCmd);
        String result = ShellExecutor.execute(adbShellCmd);
        System.out.println("[" + deviceId + "]返回 <- " + result);
        return result;
    }

    /**
     * push apk to device
     *
     * @param deviceId
     * @param localApkPath
     * @return 手机apk位置
     * @throws IOException
     */
    public static String pushApkToDevice(String deviceId, String localApkPath) throws IOException {
        String remoteApkPath = TMP_FOLDER + "autoTest.apk";
        String cmd = "adb -s " + deviceId + " push " + localApkPath + " " + remoteApkPath;
        System.out.println("[" + deviceId + "]push apk to device: " + cmd);
        ShellExecutor.execute(cmd);
        return remoteApkPath;
    }

    /**
     * 安装apk
     */
    public static void installApk(String deviceId, String remoteApkPath) throws IOException {
        String cmd = "pm install -r " + remoteApkPath;
        excuteAdbShellCmd(deviceId, cmd);
    }

    /**
     * 重启apk
     */
    public static void restartApk(String deviceId, String packageName, String launchActivity) throws IOException {
        String cmd = "am start -S -n " + packageName + "/" + launchActivity;
        excuteAdbShellCmd(deviceId, cmd);
    }

    /**
     * 清除apk数据
     */
    public static void clearApkData(String deviceId, String packageName) throws IOException {
        String cmd = "pm clear " + packageName;
        excuteAdbShellCmd(deviceId, cmd);
    }

    /**
     * 卸载apk
     */
    public static void uninstallApk(String deviceId, String packageName) throws IOException {
        String cmd = "adb -s " + deviceId + " uninstall " + packageName;
        System.out.println("[" + deviceId + "]uninstallApk: " + cmd);
        ShellExecutor.execute(cmd);
    }

    /**
     * 检查是否已安装apk
     */
    public static boolean checkApkInstalled(String deviceId, String packageName) throws IOException {
        String cmd = "pm list packages|grep " + packageName;
        String result = excuteAdbShellCmd(deviceId, cmd);
        return result != null && result.contains(packageName);
    }

    /**
     * aapt dump badging
     */
    public static String aaptDumpBadging(String apkPath) throws IOException {
        String cmd = "aapt dump badging " + apkPath;
        System.out.println(cmd);
        return ShellExecutor.execute(cmd);
    }
}
