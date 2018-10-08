package com.fgnb.android.stf.minitouch;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.fgnb.android.AndroidDevice;
import lombok.extern.slf4j.Slf4j;


/**
 * Created by jiangyitao.
 * Minitouch管理器
 */
@Slf4j
public class MinitouchManager {

    private final static String ANDROID_TMP_FOLDER = "/data/local/tmp/";
    private final static String MINITOUCH_CHMOD_SHELL = "chmod 777 %s";
    public static final String START_MINITOUCH_SHELL = ANDROID_TMP_FOLDER + "minitouch";


    private IDevice iDevice;
    private String deviceId;
    private AndroidDevice androidDevice;


    /** 运行手机minitouch的线程 Minitouch.releaseResources里杀掉minicap进程 线程将会执行结束*/
    private Thread runMinitouchThread;

    private int minitouchPort = -1;

    public MinitouchManager(AndroidDevice androidDevice){
        this.androidDevice = androidDevice;
        iDevice = androidDevice.getIDevice();
        deviceId = androidDevice.getDevice().getDeviceId();
    }

    public int getMinitouchPort() {
        return minitouchPort;
    }

    /**
     * 通过START_MINITOUCH_SHELL启动手机里的minitouch服务
     * @throws Exception
     */
    public void startMinitouch() throws Exception {

        //需要开线程启动minitouch 因为executeShellCommand(START_MINITOUCH_SHELL) 后线程会阻塞在此处
        runMinitouchThread = new Thread(() -> {
            try {
                log.info("[{}]start minitouch service，exec => {},thread id => {}",deviceId,START_MINITOUCH_SHELL,Thread.currentThread().getId());
                iDevice.executeShellCommand(START_MINITOUCH_SHELL, new NullOutputReceiver(),0);
                log.info("[{}]minitouch service stopped",deviceId);
            } catch (Exception e) {
                log.error("[{}]minitouch执行异常",deviceId,e);
            }
        });
        runMinitouchThread.start();
    }

    /**
     * 获取可用的minitouch端口
     * @return
     * @throws Exception
     */
    private int getAvailablePort() throws Exception {
        return MinitouchPortProvider.getAvailablePort();
    }

    /**
     * 端口转发到minitouch服务
     * @throws Exception
     */
    public void createForward() throws Exception {
        minitouchPort = getAvailablePort();
        try {
            iDevice.createForward(minitouchPort,"minitouch",IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        }catch (Exception e){
            log.error("[{}]createForward error,pushAvailablePort back{}  ",deviceId,minitouchPort);
            //出现异常 归还端口
            MinitouchPortProvider.pushAvailablePort(minitouchPort);
            throw e;
        }
    }


    /**
     * 安装minitouch
     * @throws Exception
     */
    public void installMinitouch() throws Exception{

        String cpuAbi = androidDevice.getDevice().getCpuAbi();

        String minitouchFilePath = "vendor/minitouch/" + cpuAbi + "/minitouch";

        log.info("[{}]push minitouchfile to phone,{}->{}",deviceId,minitouchFilePath,ANDROID_TMP_FOLDER + "minitouch");
        iDevice.pushFile(minitouchFilePath, ANDROID_TMP_FOLDER + "minitouch");

        //给手机里的minicap/minicap.so 赋予777权限
        String chmodShellCmd = String.format(MINITOUCH_CHMOD_SHELL, ANDROID_TMP_FOLDER + "minitouch");
        log.info("[{}]{} ",deviceId,chmodShellCmd);
        iDevice.executeShellCommand(chmodShellCmd,new NullOutputReceiver());
    }

}
