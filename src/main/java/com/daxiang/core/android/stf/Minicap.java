package com.daxiang.core.android.stf;

import com.android.ddmlib.*;
import com.daxiang.core.PortProvider;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.android.AndroidImgDataConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class Minicap {

    public static final String LOCAL_MINICAP_PATH = "vendor/minicap/bin/%s/minicap";
    public static final String LOCAL_MINICAP_SO_PATH = "vendor/minicap/shared/android-%d/%s/minicap.so";

    public static final String REMOTE_MINICAP_PATH = AndroidDevice.TMP_FOLDER + "/minicap";
    public static final String REMOTE_MINICAP_SO_PATH = AndroidDevice.TMP_FOLDER + "/minicap.so";

    private IDevice iDevice;
    private String deviceId;

    /**
     * 运行在手机里的进程id
     */
    private int pid;

    private boolean isRun = false;

    public Minicap(IDevice iDevice) {
        this.iDevice = iDevice;
        deviceId = iDevice.getSerialNumber();
    }

    public void setIDevice(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    /**
     * 启动minicap
     *
     * @param quality           图像质量 0-100
     * @param realResolution    设备真实分辨率 eg.1080x1920
     * @param virtualResolution minicap输出的图片分辨率 eg.1080x1920
     * @param orientation       屏幕的旋转角度
     */
    public synchronized void start(int quality, String realResolution, String virtualResolution, int orientation, AndroidImgDataConsumer androidImgDataConsumer) throws Exception {
        if (isRun) {
            return;
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                String startMinicapCmd = String.format("LD_LIBRARY_PATH=" + AndroidDevice.TMP_FOLDER + " " + REMOTE_MINICAP_PATH + " -S -Q %d -P %s@%s/%d",
                        quality,
                        realResolution,
                        virtualResolution,
                        orientation);
                log.info("[minicap][{}]启动: {}", deviceId, startMinicapCmd);
                iDevice.executeShellCommand(startMinicapCmd, new MultiLineReceiver() {
                    @Override
                    public void processNewLines(String[] lines) {
                        for (String line : lines) {
                            log.info("[minicap][{}]{}", deviceId, line);
                            if (!StringUtils.isEmpty(line) && line.startsWith("INFO: (jni/minicap/JpgEncoder.cpp")) {
                                // minicap启动完成
                                countDownLatch.countDown();
                            }
                        }
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }
                }, 0, TimeUnit.SECONDS);
                log.info("[minicap][{}]已停止运行", deviceId);
                isRun = false;
            } catch (Exception e) {
                throw new RuntimeException("minicap启动失败", e);
            }
        }).start();

        int localPort = PortProvider.getMinicapAvailablePort();

        log.info("[minicap][{}]adb forward: {} -> remote minicap", deviceId, localPort);
        iDevice.createForward(localPort, "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);

        countDownLatch.await(30, TimeUnit.SECONDS);
        log.info("[minicap][{}]minicap启动完成", deviceId);
        isRun = true;

        new Thread(() -> {
            try (Socket socket = new Socket("127.0.0.1", localPort);
                 InputStream inputStream = socket.getInputStream()) {
                log.info("[minicap][{}]创建socket获取minicap输出的数据: 127.0.0.1:{}", deviceId, localPort);

                MinicapBanner banner = new MinicapBannerParser().parse(inputStream);
                log.info("[minicap][{}]解析出Global header: {}", deviceId, banner);
                pid = banner.getPid();
                log.info("[minicap][{}]运行进程id: {}", deviceId, pid);

                MinicapFrameParser minicapFrameParser = new MinicapFrameParser();
                log.info("[minicap][{}]开始消费minicap图片数据", deviceId);
                while (true) { // 获取不到minicap输出的数据时，将会抛出MinicapFrameSizeException，循环退出
                    androidImgDataConsumer.consume(minicapFrameParser.parse(inputStream));
                }
            } catch (MinicapFrameSizeException e) {
                log.info("[minicap][{}]无法获取minicap输出数据", deviceId);
            } catch (Exception e) {
                log.error("[minicap][{}]处理minicap数据出错", deviceId, e);
            }
            log.info("[minicap][{}]已停止消费minicap图片数据", deviceId);

            // 移除adb forward
            try {
                log.info("[minicap][{}]移除adb forward: {} -> remote minicap", deviceId, localPort);
                iDevice.removeForward(localPort, "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            } catch (Exception e) {
                log.error("[minicap][{}]移除adb forward出错", deviceId, e);
            }
        }).start();
    }

    public synchronized void stop() {
        if (isRun) {
            String cmd = "kill -9 " + pid;
            try {
                log.info("[minicap][{}]kill minicap: {}", deviceId, cmd);
                iDevice.executeShellCommand(cmd, new NullOutputReceiver());
            } catch (Exception e) {
                log.error("[minicap][{}]{}执行出错", deviceId, cmd, e);
            }
        }
    }
}
