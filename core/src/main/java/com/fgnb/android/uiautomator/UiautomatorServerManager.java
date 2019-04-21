package com.fgnb.android.uiautomator;

import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class UiautomatorServerManager {


    public static final int SERVER_RUN_IN_PHONE_PROT = 9001;
    public static final String START_SERVER_CMD = "am instrument -w -e class 'com.macaca.android.testing.UIAutomatorWD' com.macaca.android.testing.test/android.support.test.runner.AndroidJUnitRunner";
    public static final String PACKAGE_NAME = "com.macaca.android.testing";

    private AndroidDevice androidDevice;
    private IDevice iDevice;
    private String deviceId;
    private int uiautomatorServerPort;

    public UiautomatorServerManager(AndroidDevice androidDevice){
        this.androidDevice = androidDevice;
        this.iDevice = androidDevice.getIDevice();
        this.deviceId = androidDevice.getDevice().getId();
    }


    /**
     * 端口转发到uiautomator2server服务
     * @throws Exception
     */
    public void createForward() throws Exception {
        uiautomatorServerPort = getAvailablePort();
        try {
            iDevice.createForward(uiautomatorServerPort,SERVER_RUN_IN_PHONE_PROT);
        }catch (Exception e){
            log.error("[{}]createForward error,pushAvailablePort back {}  ",deviceId,uiautomatorServerPort);
            throw e;
        }
    }

    /**
     * 服务是否在运行
     * @return
     */
    public boolean serverIsRunning() throws Exception{
        try {
            return AndroidUtils.isAppRunning(iDevice,PACKAGE_NAME);
        } catch (Exception e) {
            log.error("检查APP是否运行出错",e);
            throw e;
        }
    }

    public int getPort(){
        return uiautomatorServerPort;
    }

    /**
     * 开启服务
     */
    public void startServer(){
        //在开始前 先杀一次 防止在运行 导致启动出错
        try {
            AndroidUtils.forceStopApp(iDevice,PACKAGE_NAME);
        } catch (Exception e) {
            log.error("[{}]强制关闭{}出错",deviceId,PACKAGE_NAME,e);
        }
        new Thread(()->{
            CollectingOutputReceiver collectingOutputReceiver = new CollectingOutputReceiver();
            try {
                iDevice.executeShellCommand(START_SERVER_CMD, collectingOutputReceiver,0, TimeUnit.SECONDS);
                log.info("[{}]uiautomator2服务停止：{}",deviceId,collectingOutputReceiver.getOutput());
            } catch (Exception e) {
                log.error("[{}]执行{}出错",deviceId,START_SERVER_CMD,e);
            }
        }).start();
    }

    public void stopServer(){
        //1.强制杀掉APP
        try {
            AndroidUtils.forceStopApp(iDevice,PACKAGE_NAME);
        } catch (Exception e) {
            log.error("[{}]强制关闭{}出错",deviceId,PACKAGE_NAME,e);
        }
        //2.removeForward
        if(androidDevice.isConnected()) {
            try {
                log.info("[{}]removeForward : {} => {}", deviceId, uiautomatorServerPort,SERVER_RUN_IN_PHONE_PROT);
                iDevice.removeForward(uiautomatorServerPort, SERVER_RUN_IN_PHONE_PROT);
            } catch (Exception e) {
                log.error("[{}]removeForward出错", deviceId, e);
            }
        }
        log.info("[{}]uiautomator server资源回收完成",deviceId);
    }

}
