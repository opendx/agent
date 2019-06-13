package com.daxiang.android;

import com.android.ddmlib.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AndroidUtils {

    private static final String CPU_INFO_SHELL = "cat /proc/cpuinfo |grep Hardware";
    private static final String MEM_SIZE_SHELL = "cat /proc/meminfo |grep MemTotal";
    private static final String RESOLUTION_SHELL = "wm size";

    private static Map<String, String> sdkMap = new HashMap();

    static {
        sdkMap.put("8", "2.2");
        sdkMap.put("10", "2.3");
        sdkMap.put("14", "4.0");
        sdkMap.put("15", "4.0.3");
        sdkMap.put("16", "4.1.2");
        sdkMap.put("18", "4.3.1");
        sdkMap.put("19", "4.4.2");
        sdkMap.put("20", "4.4w.2");
        sdkMap.put("21", "5.0.1");
        sdkMap.put("22", "5.1.1");
        sdkMap.put("23", "6.0");
        sdkMap.put("24", "7.0");
        sdkMap.put("25", "7.1.1");
        sdkMap.put("26", "8.0");
        sdkMap.put("27", "8.1.0");
    }

    /**
     * 获取分辨率
     *
     * @param iDevice
     * @return eg.1080 X 1920
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static String getResolution(IDevice iDevice) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        return executeShellCommand(iDevice, RESOLUTION_SHELL).split(":")[1].trim();
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
        String kB = (output.replaceAll(" ", "")).replaceAll("\n", "").replaceAll("\r", "").split(":")[1];
        kB = kB.substring(0, kB.length() - 2);
        // 向上取整
        double ceil = Math.ceil(Long.parseLong(kB) / (1024.0 * 1024));
        return ceil + " GB";
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
        return sdkMap.get(getApiLevel(iDevice));
    }

    /**
     * 截屏
     *
     * @return
     */
    public static File screenshot(IDevice iDevice) throws AdbCommandRejectedException, IOException, TimeoutException {
        RawImage rawImage = iDevice.getScreenshot();
        BufferedImage image = new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_ARGB);

        int index = 0;
        int IndexInc = rawImage.bpp >> 3;

        for (int y = 0; y < rawImage.height; y++) {
            for (int x = 0; x < rawImage.width; x++) {
                int value = rawImage.getARGB(index);
                index += IndexInc;
                image.setRGB(x, y, value);
            }
        }

        File file = new File(iDevice.getSerialNumber() + ".png");
        ImageIO.write(image, "png", file);
        return file;
    }

    /**
     * 通过minicap截图
     *
     * @param iDevice
     * @param localPath  本地路径 eg. d:/path/img.jpg
     * @param resolution 手机分辨率 eg. 1080x1920
     * @throws Exception
     */
    public static void screenshotByMinicap(IDevice iDevice, String localPath, String resolution) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, SyncException {
        String imgPhonePath = "/data/local/tmp/minicap.jpg";
        String screenshotCmd = String.format("LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/0 -s >%s", resolution, resolution, imgPhonePath);
        String minicapOutput = executeShellCommand(iDevice, screenshotCmd);

        if (StringUtils.isEmpty(minicapOutput) || !minicapOutput.contains("bytes for JPG encoder")) {
            log.error("[{}]minicap截图失败, cmd: {}, minicapOutput: {}", iDevice.getSerialNumber(), screenshotCmd, minicapOutput);
            throw new RuntimeException("minicap截图失败, cmd: " + screenshotCmd + ", minicapOutput: " + minicapOutput);
        }
        // pull到本地
        iDevice.pullFile(imgPhonePath, localPath);
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
    public static String getApiLevel(IDevice iDevice) {
        return iDevice.getProperty("ro.build.version.sdk");
    }

    /**
     * 等待设备上线
     */
    public static void waitForDeviceOnline(IDevice iDevice, long maxWaitTimeSeconds) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime > maxWaitTimeSeconds * 1000) {
                throw new RuntimeException("[" + iDevice.getSerialNumber() + "]设备未上线");
            }
            if (iDevice.isOnline()) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //ignore
            }
        }
    }

    /**
     * 强制关闭app
     *
     * @param iDevice
     * @param packageName
     */
    public static void forceStopApp(IDevice iDevice, String packageName) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        iDevice.executeShellCommand("am force-stop " + packageName, new NullOutputReceiver());
    }

    /**
     * input keyevent
     * keyCode对照表 https://blog.csdn.net/moyu123456789/article/details/71209893
     *
     * @param keyCode
     */
    public static void inputKeyevent(IDevice iDevice, int keyCode) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        iDevice.executeShellCommand("input keyevent " + keyCode, new NullOutputReceiver());
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

}
