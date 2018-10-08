package com.fgnb.actions.utils;

/**
 * Created by jiangyitao.
 */
public class AndroidUtil {

    public static final String DEVICE_FILE_PATH = "/data/local/tmp";
    /**
     * 执行命令
     * @param cmd
     * @throws Exception
     */
    public static void excuteCmd(String deviceId,String cmd) throws Exception{
        System.out.println("["+deviceId+"]执行 -> "+cmd);
        String result = ShellExecutor.execReturnResult("adb -s " + deviceId + " shell \"" + cmd + "\"");
        System.out.println("["+deviceId+"]返回 <- "+result);
    }

    /**
     * push APK到手机
     * @param deviceId
     * @param localAppPath
     * @throws Exception
     */
    public static String pushAppToDeviceByAdbShell(String deviceId,String localAppPath) throws Exception{
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
     * @throws Exception
     */
    public static void installAppByAdbShell(String deviceId,String phoneAppPath) throws Exception {
        String cmd = "pm install -t "+phoneAppPath;
        System.out.println("["+deviceId+"]安装android app:"+cmd);
        excuteCmd(deviceId,cmd);
    }

    /**
     * 重启APP
     * @param deviceId
     * @param packageName
     * @param launchActivity
     * @throws Exception
     */
    public static void restartAppByAdbShell(String deviceId,String packageName,String launchActivity) throws Exception {
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
    public static void clearAppData(String deviceId,String packageName) throws Exception {
        String cmd = "pm clear "+packageName;
        System.out.println("["+deviceId+"]清除APP数据:"+cmd);
        excuteCmd(deviceId,cmd);
    }

    /**
     * 卸载Android APP
     * @param deviceId
     * @param appPackage
     * @throws Exception
     */
    public static void uninstallAppByAdbShell(String deviceId,String appPackage) throws Exception {
        String cmd = "adb -s "+deviceId + " uninstall "+appPackage;
        System.out.println("["+deviceId+"]卸载android app:"+cmd);
        ShellExecutor.exec(cmd);
    }

    /**
     * 检查安卓手机是否安装了APP
     * @param deviceId
     * @param appPackage
     * @return
     * @throws Exception
     */
    public static boolean isInstalledApp(String deviceId,String appPackage) throws Exception {
        String cmd = "adb -s " + deviceId + " shell \"pm list packages|grep " + appPackage + "\"";
        System.out.println("[" + deviceId + "]检查是否安装了APP:" + cmd);
        String result = ShellExecutor.execReturnResult(cmd);
        System.out.println("[" + deviceId + "]"+result);
        if (result != null && result.contains(appPackage)) {
            return true;
        }
        return false;
    }
}
