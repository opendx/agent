package com.daxiang.core.mobile.android.stf;

import com.android.ddmlib.*;
import com.daxiang.core.PortProvider;
import com.daxiang.core.mobile.android.AndroidDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.function.Consumer;

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
    private String mobileId;

    /**
     * 运行在Mobile里的进程id
     */
    private int pid;

    private boolean isRunning = false;

    public Minicap(IDevice iDevice) {
        this.iDevice = iDevice;
        mobileId = iDevice.getSerialNumber();
    }

    public void setIDevice(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    /**
     * 启动minicap
     *
     * @param quality           图像质量 0-100
     * @param realResolution    Mobile真实分辨率 eg.1080x1920
     * @param virtualResolution minicap输出的图片分辨率 eg.1080x1920
     * @param orientation       屏幕的旋转角度
     */
    public synchronized void start(int quality, String realResolution, String virtualResolution,
                                   int orientation, Consumer<ByteBuffer> consumer) throws Exception {
        if (isRunning) {
            return;
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                String startMinicapCmd = String.format("LD_LIBRARY_PATH=%s %s -S -Q %d -P %s@%s/%d",
                        AndroidDevice.TMP_FOLDER,
                        REMOTE_MINICAP_PATH,
                        quality,
                        realResolution,
                        virtualResolution,
                        orientation);
                log.info("[{}]start minicap: {}", mobileId, startMinicapCmd);
                iDevice.executeShellCommand(startMinicapCmd, new MultiLineReceiver() {
                    @Override
                    public void processNewLines(String[] lines) {
                        for (String line : lines) {
                            log.info("[{}]minicap: {}", mobileId, line);
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
                log.info("[{}]minicap已停止运行", mobileId);
                isRunning = false;
            } catch (Exception e) {
                throw new RuntimeException(String.format("[%s]minicap启动失败", mobileId), e);
            }
        }).start();

        int minicapStartTimeoutInSeconds = 30;
        boolean minicapStartSuccess = countDownLatch.await(minicapStartTimeoutInSeconds, TimeUnit.SECONDS);
        if (!minicapStartSuccess) {
            throw new RuntimeException(String.format("[%s]minicap启动失败，超时时间：%d秒", mobileId, minicapStartTimeoutInSeconds));
        }

        log.info("[{}]minicap启动完成", mobileId);
        isRunning = true;

        int localPort = PortProvider.getMinicapAvailablePort();

        log.info("[{}]adb forward: {} -> remote minicap", mobileId, localPort);
        iDevice.createForward(localPort, "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);

        new Thread(() -> {
            try (Socket socket = new Socket("127.0.0.1", localPort);
                 InputStream inputStream = socket.getInputStream()) {

                MinicapBanner banner = new MinicapBannerParser().parse(inputStream);
                log.info("[{}]minicap banner: {}", mobileId, banner);
                pid = banner.getPid();
                log.info("[{}]minicap pid: {}", mobileId, pid);

                MinicapFrameParser minicapFrameParser = new MinicapFrameParser();
                while (true) { // 获取不到minicap输出的数据时，将会抛出MinicapFrameSizeException，循环退出
                    consumer.accept(minicapFrameParser.parse(inputStream));
                }
            } catch (MinicapFrameSizeException e) {
                log.info("[{}]无法获取minicap输出数据", mobileId);
            } catch (Exception e) {
                log.error("[{}]处理minicap数据失败", mobileId, e);
            }
            log.info("[{}]已停止消费minicap图片数据", mobileId);

            // 移除adb forward
            try {
                log.info("[{}]移除adb forward: {} -> remote minicap", mobileId, localPort);
                iDevice.removeForward(localPort, "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            } catch (Exception e) {
                log.error("[{}]移除adb forward出错", mobileId, e);
            }
        }).start();
    }

    public synchronized void stop() {
        if (isRunning) {
            String cmd = "kill -9 " + pid;
            try {
                log.info("[{}]kill minicap: {}", mobileId, cmd);
                iDevice.executeShellCommand(cmd, new NullOutputReceiver());
            } catch (Exception e) {
                log.error("[{}]{}执行出错", mobileId, cmd, e);
            }
        }
    }
}
