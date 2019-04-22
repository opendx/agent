package com.fgnb.android.stf.minicap;

import com.android.ddmlib.*;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.PortProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class Minicap {

    private static final String START_MINICAP_SHELL = "LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/%d";

    private BlockingQueue<byte[]> imgQueue = new LinkedBlockingQueue<>();

    private AndroidDevice androidDevice;
    private String deviceId;
    /**
     * 本地端口，adb forward 本地端口到手机端口
     */
    private int localPort;
    /**
     * 运行在手机里的进程id
     */
    private int pid;
    /**
     * 是否持续解析minicap数据
     */
    private boolean isParseFrame = false;

    public Minicap(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
        deviceId = androidDevice.getId();
    }

    public BlockingQueue<byte[]> getImgQueue() {
        return imgQueue;
    }

    public boolean isParseFrame() {
        return isParseFrame;
    }

    /**
     * 启动minicap
     *
     * @param virtualResolution minicap输出的图片分辨率 eg.1080x1920
     * @param orientation       屏幕的旋转角度
     */
    public void start(String virtualResolution, Integer orientation) throws Exception {
        String startMinicapCmd = String.format(START_MINICAP_SHELL, androidDevice.getResolution(), virtualResolution, orientation);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        //启动minicap会阻塞线程，启一个线程运行minicap
        new Thread(() -> {
            try {
                log.info("[{}][minicap]启动：{}", deviceId, startMinicapCmd);
                androidDevice.getIDevice().executeShellCommand(startMinicapCmd, new MultiLineReceiver() {
                    @Override
                    public void processNewLines(String[] lines) {
                        for (String line : lines) {
                            log.info("[{}][minicap]手机控制台输出：{}", deviceId, line);
                            if (!StringUtils.isEmpty(line) && line.startsWith("INFO: (jni/minicap/JpgEncoder.cpp)")) {
                                //minicap启动完成
                                countDownLatch.countDown();
                            }
                        }
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }
                }, 0, TimeUnit.SECONDS);
                log.info("[{}][minicap]已停止运行", deviceId);
            } catch (Exception e) {
                log.error("[{}][minicap]启动出错", deviceId, e);
            }
        }).start();

        this.localPort = PortProvider.getMinicapAvailablePort();

        androidDevice.getIDevice().createForward(localPort, "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        log.info("[{}][minicap]adb forward: {} -> remote minicap", deviceId, localPort);

        countDownLatch.await();
        log.info("[{}][minicap]minicap启动完成", deviceId);

        new Thread(() -> {
            try (Socket socket = new Socket("127.0.0.1", localPort);
                 InputStream inputStream = socket.getInputStream()) {
                log.info("[{}][minicap]创建socket获取minicap输出的数据：127.0.0.1:{}", deviceId, localPort);

                MinicapBanner banner = MinicapBannerParser.parse(inputStream);
                log.info("[{}][minicap]解析出Global header：{}", deviceId, banner);
                pid = banner.getPid();
                log.info("[{}][minicap]运行进程id: {}", deviceId, pid);

                log.info("[{}][minicap]开始持续向imgQueue推送图片数据", deviceId);
                this.isParseFrame = true;
                while (isParseFrame) {
                    byte[] img = MinicapFrameParser.parse(inputStream);
                    imgQueue.offer(img);
                }
                log.info("[{}][minicap]已停止向imgQueue推送图片数据", deviceId);

                // 手机未连接，minicap会自己退出
                if (pid > 0 && androidDevice.isConnected()) {
                    String cmd = "kill -9 " + pid;
                    log.info("[{}][minicap]kill minicap：{}", deviceId, cmd);
                    try {
                        androidDevice.getIDevice().executeShellCommand(cmd, new NullOutputReceiver());
                    } catch (Exception e) {
                        log.error("[{}][minicap]{}执行出错", deviceId, cmd, e);
                    }
                }

                //手机未连接 adb forward会自己移除
                if (androidDevice.isConnected()) {
                    try {
                        log.info("[{}][minicap]移除adb forward: {} -> remote minicap", deviceId, localPort);
                        androidDevice.getIDevice().removeForward(localPort, "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
                    } catch (Exception e) {
                        log.error("[{}][minicap]移除adb forward出错", deviceId, e);
                    }
                }

                log.info("[{}][minicap]清空imgQueue数据", deviceId);
                imgQueue.clear();
            } catch (IOException e) {
                log.error("[{}][minicap]处理minicap数据出错", deviceId, e);
            }
        }).start();
    }

    public void stop() {
        log.info("[{}][minicap]开始停止minicap", deviceId);
        isParseFrame = false;
    }

}
