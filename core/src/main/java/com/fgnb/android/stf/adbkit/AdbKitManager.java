package com.fgnb.android.stf.adbkit;

import com.fgnb.utils.ShellExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ExecuteWatchdog;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AdbKitManager {

    private int port;
    private String deviceId;

    private static final String START_CMD = "node vendor/adbkit/bin/adbkit usb-device-to-tcp -p %d %s";

    private ExecuteWatchdog watchdog;

    /** 运行adbkit的线程 stopUsbDeviceToTcp 线程将会执行结束 */
    private Thread startAdbKitThread;

    public AdbKitManager(int port,String deviceId){
        this.port = port;
        this.deviceId = deviceId;
    }

    public void startUsbDeviceToTcp(){
        startAdbKitThread = new Thread(()->{
            try {
                String cmd = String.format(START_CMD, port, deviceId);
                log.info("[{}]启动adbkit，开启远程调试功能,cmd=>{}",deviceId,cmd);
                watchdog = ShellExecutor.execReturnWatchdog(cmd);
            } catch (IOException e) {
                log.error("[{}]adbkit运行异常",deviceId,e);
                //归还端口
                AdbKitPortProvider.pushAvailablePort(port);
            }
        });
        startAdbKitThread.start();
    }

    public void stopUsbDeviceToTcp(){
        log.info("[{}]开始关闭adbkit，端口:{}",deviceId,port);
        if(watchdog != null) {
            //杀掉startUsbDeviceToTcp 启动的START_CMD命令
            watchdog.destroyProcess();
        }
        //归还端口
        log.info("[{}]归还adbkit端口{}",deviceId,port);
        AdbKitPortProvider.pushAvailablePort(port);

        log.info("[{}]adbkit资源回收完成",deviceId);
    }


}
