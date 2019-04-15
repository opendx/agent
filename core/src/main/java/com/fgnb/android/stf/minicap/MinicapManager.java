package com.fgnb.android.stf.minicap;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidUtils;
import com.fgnb.App;
import lombok.extern.slf4j.Slf4j;


/**
 * Created by jiangyitao.
 * Minicap管理器
 */
@Slf4j
public class MinicapManager {

    private static final String START_MINICAP_SHELL = "LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/0";
    private static final String ANDROID_TMP_FOLDER = "/data/local/tmp/";
    private static final String MINICAP_CHMOD_SHELL = "chmod 777 %s %s";

    private AndroidDevice androidDevice;
    private String deviceId;
    private IDevice iDevice;


    /**
     * 在网页上手机显示的高度
     */
    private int screenDisplayHeight = Integer.parseInt(App.getProperty("screenDisplayHeight"));

    /**
     * 运行手机minicap的线程 MinicapDataHandler.releaseResources里杀掉minicap进程 线程将会执行结束
     */
    private Thread runMinicapThread;

    /**
     * minicap端口
     */
    private int minicapPort = -1;

    public MinicapManager(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
        iDevice = androidDevice.getIDevice();
        deviceId = androidDevice.getDevice().getId();
    }

    /**
     * 开启安卓手机里的minicap输出屏幕数据
     *
     * @return
     * @throws Exception
     */
    public void startMiniCap() throws Exception {

        //实际屏幕分辨率
        String actualDeviceResolution = androidDevice.getResolution();

        //在网页上显示的分辨率
        String screenDisplayResolution = getScreenDisplayResolution();

        String minicapStartCmd = String.format(START_MINICAP_SHELL, actualDeviceResolution, screenDisplayResolution);
        //需要开线程启动minicap 因为executeShellCommand(START_MINICAP_SHELL) 后线程会阻塞在此处
        runMinicapThread = new Thread(() -> {
            try {
                log.info("[{}]start minicap service，exec => {},thread id => {}", iDevice.getSerialNumber(), minicapStartCmd, Thread.currentThread().getId());
                iDevice.executeShellCommand(minicapStartCmd, new NullOutputReceiver(), 0);
                log.info("[{}]minicap service stopped", iDevice);
            } catch (Exception e) {
                log.error("minicap执行异常", e);
            }
        });
        runMinicapThread.start();
    }

    /**
     * 获取可用的minicap端口
     *
     * @return
     * @throws Exception
     */
    private int getAvailablePort() throws Exception {
        return MinicapPortProvider.getAvailablePort();
    }

    /**
     * 端口转发到minicap服务
     *
     * @throws Exception
     */
    public void createForward() throws Exception {
        minicapPort = getAvailablePort();
        try {
            iDevice.createForward(minicapPort, "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        } catch (Exception e) {
            //出现异常 归还端口
            log.error("[{}]createForward error,pushAvailablePort back{}  ", deviceId, minicapPort);
            MinicapPortProvider.pushAvailablePort(minicapPort);
            throw e;
        }

    }

    public int getMinicapPort() {
        return minicapPort;
    }

    /**
     * 根据手机cpu架构/android版本 push相应的minicap文件到手机/data/local/tmp目录
     * 对minicap/minicap.so 文件赋予777权限
     *
     * @throws Exception
     */
    public void installMinicap() throws Exception {

        String cpuAbi = AndroidUtils.getCpuAbi(iDevice);
        String apiLevel = AndroidUtils.getApiLevel(iDevice);

        String minicapFilePath = "vendor/minicap/bin/" + cpuAbi + "/minicap";
        String minicapSoFilePath = "vendor/minicap/shared/android-" + apiLevel + "/" + cpuAbi + "/minicap.so";

        //push minicap/minicap.so 到手机
        log.info("[{}]push minicapfile to phone,{}->{}", deviceId, minicapFilePath, ANDROID_TMP_FOLDER + "minicap");
        iDevice.pushFile(minicapFilePath, ANDROID_TMP_FOLDER + "minicap");
        log.info("[{}]push minicapsofile to phone,{}->{}", deviceId, minicapSoFilePath, ANDROID_TMP_FOLDER + "minicap.so");
        iDevice.pushFile(minicapSoFilePath, ANDROID_TMP_FOLDER + "minicap.so");

        //给手机里的minicap/minicap.so 赋予777权限
        String chmodShellCmd = String.format(MINICAP_CHMOD_SHELL, ANDROID_TMP_FOLDER + "minicap", ANDROID_TMP_FOLDER + "minicap.so");
        log.info("[{}]{} ", deviceId, chmodShellCmd);
        iDevice.executeShellCommand(chmodShellCmd, new NullOutputReceiver());

    }

    /**
     * 获取屏幕在网页上显示的分辨率
     *
     * @return
     */
    private String getScreenDisplayResolution() throws Exception {
        //比例
        double scale = (double) screenDisplayHeight / androidDevice.getDevice().getScreenHeight();
        int screenDisplayWidth = (int) Math.round(androidDevice.getDevice().getScreenWidth() * scale);
        return screenDisplayWidth + "x" + screenDisplayHeight;
    }

}
