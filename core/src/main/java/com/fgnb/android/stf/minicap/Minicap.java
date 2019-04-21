package com.fgnb.android.stf.minicap;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.fgnb.android.AndroidDevice;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class Minicap {

    private static final String START_MINICAP_SHELL = "LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/%d";

    /**
     * 本地端口，adb forward 本地端口到手机端口
     */
    private Integer localPort;
    /**
     * 运行在手机里的进程id
     */
    private Integer pid;

    private Socket socket;
    private DataInputStream dataInputStream;
    private Queue<byte[]> imgDataQueue = new ConcurrentLinkedQueue();

    private AndroidDevice androidDevice;

    public Minicap(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
    }

    /**
     * 启动minicap
     *
     * @param virtualResolution minicap输出的图片分辨率 eg.1080x1920
     * @param orientation       屏幕的旋转角度
     */
    public void start(String virtualResolution, Integer orientation) throws Exception {
        String startMinicapCmd = String.format(START_MINICAP_SHELL, androidDevice.getResolution(), virtualResolution, orientation);
        //启动minicap会阻塞线程，启一个线程运行minicap
        new Thread(() -> {
            try {
                log.info("[{}][minicap]启动：{}", getDeviceId(), startMinicapCmd);
                androidDevice.getIDevice().executeShellCommand(startMinicapCmd, new NullOutputReceiver(), 0, TimeUnit.SECONDS);
                log.info("[{}][minicap]停止运行", getDeviceId());
            } catch (Exception e) {
                log.error("[{}][minicap]启动出错", getDeviceId(), e);
            }
        }).start();


        //todo 申请一个可用的端口 localPort = xx;

        androidDevice.getIDevice().createForward(localPort, "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        log.info("[{}][minicap]端口转发：{} -> remote minicap", getDeviceId(), localPort);

        socket = new Socket("127.0.0.1", localPort);
        log.info("[{}][minicap]创建socket：127.0.0.1:{}",getDeviceId(),localPort);

        dataInputStream = new DataInputStream(socket.getInputStream());

        new Thread(() -> {

        }).start();
    }

    public void stop() {
        if (pid == null) {
            return;
        }


    }

    private String getDeviceId() {
        return androidDevice.getId();
    }
}
