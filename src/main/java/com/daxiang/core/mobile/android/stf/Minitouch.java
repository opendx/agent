package com.daxiang.core.mobile.android.stf;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.NullOutputReceiver;
import com.daxiang.core.mobile.android.AndroidDevice;
import com.daxiang.core.PortProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class Minitouch {

    public static final String LOCAL_MINITOUCH_PATH = "vendor/minitouch/%s/minitouch";
    public static final String REMOTE_MINITOUCH_PATH = AndroidDevice.TMP_FOLDER + "/minitouch";

    private IDevice iDevice;
    private String mobileId;

    /**
     * https://github.com/openstf/minitouch#-max-contacts-max-x-max-y-max-pressure
     */
    private int width;
    /**
     * https://github.com/openstf/minitouch#-max-contacts-max-x-max-y-max-pressure
     */
    private int height;

    /**
     * 运行在Mobile里的进程id
     */
    private int pid;

    /**
     * 用于向minitouch发送指令
     */
    private PrintWriter printWriter;

    private boolean isRunning = false;


    public Minitouch(IDevice iDevice) {
        this.iDevice = iDevice;
        mobileId = iDevice.getSerialNumber();
    }

    public void setIDevice(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    /**
     * 开始运行minitouch
     *
     * @throws Exception
     */
    public synchronized void start() throws Exception {
        if (isRunning) {
            return;
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                log.info("[{}]启动minitouch: {}", mobileId, REMOTE_MINITOUCH_PATH);
                iDevice.executeShellCommand(REMOTE_MINITOUCH_PATH, new MultiLineReceiver() {
                    @Override
                    public void processNewLines(String[] lines) {
                        for (String line : lines) {
                            log.info("[{}]minitouch: {}", mobileId, line);
                            if (!StringUtils.isEmpty(line) && line.startsWith("Type")) {
                                // minitouch启动完成
                                countDownLatch.countDown();
                            }
                        }
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }
                }, 0, TimeUnit.SECONDS);
                log.info("[{}]minitouch已停止运行", mobileId);
                isRunning = false;
            } catch (Exception e) {
                throw new RuntimeException(String.format("[%s]启动minitouch失败", mobileId), e);
            }
        }).start();

        int minitouchStartTimeoutInSeconds = 30;
        boolean minitouchStartSuccess = countDownLatch.await(minitouchStartTimeoutInSeconds, TimeUnit.SECONDS);
        if (!minitouchStartSuccess) {
            throw new RuntimeException(String.format("[%s]启动minitouch失败，超时时间：%d秒", mobileId, minitouchStartTimeoutInSeconds));
        }

        log.info("[{}]minitouch启动完成", mobileId);
        isRunning = true;

        int localPort = PortProvider.getMinitouchAvailablePort();

        log.info("[{}]adb forward: {} -> remote minitouch", mobileId, localPort);
        iDevice.createForward(localPort, "minitouch", IDevice.DeviceUnixSocketNamespace.ABSTRACT);

        new Thread(() -> {
            try (Socket socket = new Socket("127.0.0.1", localPort);
                 BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter printWriter = new PrintWriter(socket.getOutputStream())) {
                this.printWriter = printWriter;

                String line;
                while ((line = br.readLine()) != null) {
                    // https://github.com/openstf/minitouch#readable-from-the-socket
                    if (line.startsWith("^")) {
                        // ^ <max-contacts> <max-x> <max-y> <max-pressure>
                        // ^ 10 1079 1919 2048
                        String[] content = line.split(" ");
                        width = Integer.parseInt(content[2]);
                        height = Integer.parseInt(content[3]);
                        log.info("[{}]minitouch width: {} height: {}", mobileId, width, height);
                    } else if (line.startsWith("$")) {
                        // $ 12310
                        pid = Integer.parseInt(line.split(" ")[1]);
                        log.info("[{}]minitouch pid: {}", mobileId, pid);
                    }
                }
            } catch (Exception e) {
                log.error("[{}]处理minitouch数据失败", mobileId, e);
            }

            if (printWriter != null) {
                printWriter.close();
            }

            try {
                log.info("[{}]移除adb forward: {} -> remote minitouch", mobileId, localPort);
                iDevice.removeForward(localPort, "minitouch", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            } catch (Exception e) {
                log.error("[{}]移除adb forward出错", mobileId, e);
            }
        }).start();

    }

    /**
     * 停止运行monitouch
     */
    public synchronized void stop() {
        if (isRunning) {
            String cmd = "kill -9 " + pid;
            try {
                log.info("[{}]kill minitouch: {}", mobileId, cmd);
                iDevice.executeShellCommand(cmd, new NullOutputReceiver());
            } catch (Exception e) {
                log.error("[{}]{}执行出错", mobileId, cmd, e);
            }
        }
    }

    /**
     * 按下 https://github.com/openstf/minitouch#d-contact-x-y-pressure
     *
     * @param x
     * @param y
     */
    public void touchDown(int x, int y, int screenWidth, int screenHeight) {
        int minitouchX = (int) (((float) x) / screenWidth * width);
        int minitouchY = (int) (((float) y) / screenHeight * height);
        commit(String.format("d 0 %d %d 50", minitouchX, minitouchY));
    }

    /**
     * 松手 https://github.com/openstf/minitouch#u-contact
     */
    public void touchUp() {
        commit("u 0");
    }

    /**
     * 滑动 https://github.com/openstf/minitouch#m-contact-x-y-pressure
     */
    public void moveTo(int x, int y, int screenWidth, int screenHeight) {
        int minitouchX = (int) (((float) x) / screenWidth * width);
        int minitouchY = (int) (((float) y) / screenHeight * height);
        commit(String.format("m 0 %d %d 50", minitouchX, minitouchY));
    }

    /**
     * 提交minitouch命令
     *
     * @param cmd
     */
    private void commit(String cmd) {
        if (printWriter != null) {
            printWriter.write(cmd + "\nc\n");
            printWriter.flush();
        }
    }
}
