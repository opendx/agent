package com.fgnb.android.stf.minitouch;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidUtils;
import com.fgnb.android.PortProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;


/**
 * Created by jiangyitao.
 * Minitouch管理器
 */
@Slf4j
public class MinitouchManager {

    private final static String ANDROID_TMP_FOLDER = "/data/local/tmp/";
    public static final String START_MINITOUCH_SHELL = ANDROID_TMP_FOLDER + "minitouch";


    private IDevice iDevice;
    private String deviceId;
    private AndroidDevice androidDevice;


    private int minitouchPort = -1;

    public MinitouchManager(AndroidDevice androidDevice){
        this.androidDevice = androidDevice;
        iDevice = androidDevice.getIDevice();
        deviceId = androidDevice.getDevice().getId();
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
        new Thread(() -> {
            try {
                log.info("[{}]start minitouch service，exec => {},thread id => {}",deviceId,START_MINITOUCH_SHELL,Thread.currentThread().getId());
                iDevice.executeShellCommand(START_MINITOUCH_SHELL, new NullOutputReceiver(),0, TimeUnit.SECONDS);
                log.info("[{}]minitouch service stopped",deviceId);
            } catch (Exception e) {
                log.error("[{}]minitouch执行异常",deviceId,e);
            }
        }).start();
    }


    /**
     * 端口转发到minitouch服务
     * @throws Exception
     */
    public void createForward() throws Exception {
        minitouchPort = PortProvider.getMinitouchAvailablePort();
        try {
            iDevice.createForward(minitouchPort,"minitouch",IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        }catch (Exception e){
            log.error("[{}]createForward error,pushAvailablePort back{}  ",deviceId,minitouchPort);
            throw e;
        }
    }



}
