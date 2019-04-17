package com.fgnb.android.stf.minicap;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.fgnb.android.AndroidDevice;
import com.fgnb.App;
import lombok.extern.slf4j.Slf4j;


/**
 * Created by jiangyitao.
 * Minicap管理器
 */
@Slf4j
public class MinicapManager {

    private static final String START_MINICAP_SHELL = "LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/0";


    private AndroidDevice androidDevice;

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
                log.info("[{}]start minicap service，exec => {},thread id => {}", androidDevice.getIDevice().getSerialNumber(), minicapStartCmd, Thread.currentThread().getId());
                androidDevice.getIDevice().executeShellCommand(minicapStartCmd, new NullOutputReceiver(), 0);
                log.info("[{}]minicap service stopped", androidDevice.getDevice().getId());
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
            androidDevice.getIDevice().createForward(minicapPort, "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        } catch (Exception e) {
            //出现异常 归还端口
            log.error("[{}]createForward error,pushAvailablePort back{}  ", androidDevice.getDevice().getId(), minicapPort);
            MinicapPortProvider.pushAvailablePort(minicapPort);
            throw e;
        }

    }

    public int getMinicapPort() {
        return minicapPort;
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
