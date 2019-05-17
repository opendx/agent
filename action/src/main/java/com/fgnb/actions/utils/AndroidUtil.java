package com.fgnb.actions.utils;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class AndroidUtil {

    public static final String DEVICE_FILE_PATH = "/data/local/tmp";
    /**
     * 执行命令
     * @param cmd
     */
    public static void excuteCmd(String deviceId,String cmd) throws IOException {
        System.out.println("["+deviceId+"]执行 -> "+cmd);
        String result = ShellExecutor.execReturnResult("adb -s " + deviceId + " shell \"" + cmd + "\"");
        System.out.println("["+deviceId+"]返回 <- "+result);
    }

    /**
     * push APK到手机
     * @param deviceId
     * @param localAppPath
     */
    public static String pushAppToDeviceByAdbShell(String deviceId,String localAppPath) throws IOException {
        String remoteApkPath = DEVICE_FILE_PATH+"/autoTest.apk";
        String cmd = "adb -s " + deviceId + " push "+localAppPath+" "+remoteApkPath;
        System.out.println("["+deviceId+"]push APP到手机:"+cmd);
        ShellExecutor.exec(cmd);
        return remoteApkPath;
    }

    /**
     * 安装Android APP
     * @param deviceId
     * @param phoneAppPath
     */
    public static void installAppByAdbShell(String deviceId,String phoneAppPath) throws IOException {
        String cmd = "pm install -t "+phoneAppPath;
        System.out.println("["+deviceId+"]安装android app:"+cmd);
        excuteCmd(deviceId,cmd);
    }

    /**
     * 重启APP
     * @param deviceId
     * @param packageName
     * @param launchActivity
     */
    public static void restartAppByAdbShell(String deviceId,String packageName,String launchActivity) throws IOException {
        String cmd = "am start -S -n "+packageName+"/"+launchActivity;
        System.out.println("["+deviceId+"]重启APP:"+cmd);
        excuteCmd(deviceId,cmd);
    }

    /**
     * 清除APP数据
     * @param deviceId
     * @param packageName
     * @throws Exception
     */
    public static void clearAppData(String deviceId,String packageName) throws IOException {
        String cmd = "pm clear "+packageName;
        System.out.println("["+deviceId+"]清除APP数据:"+cmd);
        excuteCmd(deviceId,cmd);
    }

    /**
     * 卸载Android APP
     * @param deviceId
     * @param appPackage
     */
    public static void uninstallAppByAdbShell(String deviceId,String appPackage) throws IOException {
        String cmd = "adb -s "+deviceId + " uninstall "+appPackage;
        System.out.println("["+deviceId+"]卸载android app:"+cmd);
        ShellExecutor.exec(cmd);
    }

    /**
     * 检查安卓手机是否安装了APP
     * @param deviceId
     * @param appPackage
     * @return
     */
    public static boolean isInstalledApp(String deviceId,String appPackage) throws IOException {
        String cmd = "adb -s " + deviceId + " shell \"pm list packages|grep " + appPackage + "\"";
        System.out.println("[" + deviceId + "]检查是否安装了APP:" + cmd);
        String result = ShellExecutor.execReturnResult(cmd);
        System.out.println("[" + deviceId + "]"+result);
        return result != null && result.contains(appPackage);
    }
}
