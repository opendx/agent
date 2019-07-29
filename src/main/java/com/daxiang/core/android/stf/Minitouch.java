package com.daxiang.core.android.stf;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.NullOutputReceiver;
import com.daxiang.core.android.AndroidDevice;
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

    public static final String START_MINITOUCH_CMD = AndroidDevice.TMP_FOLDER + "minitouch";

    private int localPort;

    private AndroidDevice androidDevice;
    private String deviceId;

    /**
     * https://github.com/openstf/minitouch#-max-contacts-max-x-max-y-max-pressure
     */
    private int width;
    /**
     * https://github.com/openstf/minitouch#-max-contacts-max-x-max-y-max-pressure
     */
    private int height;

    /**
     * 运行在手机里的进程id
     */
    private int pid;

    /**
     * 用于向minitouch发送指令
     */
    private PrintWriter printWriter;


    public Minitouch(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
        deviceId = androidDevice.getId();
    }

    /**
     * 开始运行minitouch
     *
     * @throws Exception
     */
    public void start() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        // 启动minitouch会阻塞线程，启一个线程运行minitouch
        new Thread(() -> {
            try {
                log.info("[{}][minitouch]启动：{}", deviceId, START_MINITOUCH_CMD);
                androidDevice.getIDevice().executeShellCommand(START_MINITOUCH_CMD, new MultiLineReceiver() {
                    @Override
                    public void processNewLines(String[] lines) {
                        for (String line : lines) {
                            log.info("[{}][minitouch]手机控制台输出：{}", deviceId, line);
                            if (!StringUtils.isEmpty(line) && line.startsWith("Type")) {
                                //minitouch启动完成
                                countDownLatch.countDown();
                            }
                        }
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }
                }, 0, TimeUnit.SECONDS);
                log.info("[{}][minitouch]已停止运行", deviceId);
            } catch (Exception e) {
                log.error("[{}][minitouch]启动出错", deviceId, e);
            }
        }).start();

        this.localPort = PortProvider.getMinitouchAvailablePort();

        log.info("[{}][minitouch]adb forward: {} -> remote minitouch", deviceId, localPort);
        androidDevice.getIDevice().createForward(localPort, "minitouch", IDevice.DeviceUnixSocketNamespace.ABSTRACT);

        countDownLatch.await();
        log.info("[{}][minitouch]minitouch启动完成", deviceId);

        new Thread(() -> {
            try (Socket socket = new Socket("127.0.0.1", localPort);
                 BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter printWriter = new PrintWriter(socket.getOutputStream())) {
                log.info("[{}][minitouch]创建socket获取minitouch输出的数据：127.0.0.1:{}", deviceId, localPort);
                this.printWriter = printWriter;

                String line;
                while ((line = br.readLine()) != null) {
                    log.info("[{}][minitouch]socket获取：{}", deviceId, line);
                    // https://github.com/openstf/minitouch#readable-from-the-socket
                    if (line.startsWith("^")) {
                        // ^ <max-contacts> <max-x> <max-y> <max-pressure>
                        // ^ 10 1079 1919 2048
                        String[] content = line.split(" ");
                        width = Integer.parseInt(content[2]);
                        height = Integer.parseInt(content[3]);
                        log.info("[{}][minitouch]minitouch输出设备宽度:{},高度:{}", deviceId, width, height);
                    } else if (line.startsWith("$")) {
                        // $ 12310
                        pid = Integer.parseInt(line.split(" ")[1]);
                        log.info("[{}][minitouch]运行进程id: {}", deviceId, pid);
                    }
                }
            } catch (Exception e) {
                log.error("[{}][minitouch]处理minitouch数据出错", deviceId, e);
            }

            if (printWriter != null) {
                printWriter.close();
            }

            //手机未连接 adb forward会自己移除
            if (androidDevice.isConnected()) {
                try {
                    log.info("[{}][minitouch]移除adb forward: {} -> remote minitouch", deviceId, localPort);
                    androidDevice.getIDevice().removeForward(localPort, "minitouch", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
                } catch (Exception e) {
                    log.error("[{}][minitouch]移除adb forward出错", deviceId, e);
                }
            }
        }).start();

    }

    /**
     * 停止运行monitouch
     */
    public void stop() {
        log.info("[{}][minitouch]开始停止minitouch", deviceId);

        //手机未连接，minitouch会自己退出
        if (pid > 0 && androidDevice.isConnected()) {
            String cmd = "kill -9 " + pid;
            log.info("[{}][minitouch]kill minitouch：{}", deviceId, cmd);
            try {
                androidDevice.getIDevice().executeShellCommand(cmd, new NullOutputReceiver());
            } catch (Exception e) {
                log.error("[{}][minitouch]{}执行出错", deviceId, cmd, e);
            }
        }

    }

    /**
     * 按下
     *
     * @param percentX 屏幕X百分比
     * @param percentY 屏幕Y百分比
     */
    public void touchDown(float percentX, float percentY) {
        touchDown((int) (percentX * width), (int) (percentY * height));
    }

    /**
     * 滑动
     *
     * @param percentX 屏幕X百分比
     * @param percentY 屏幕Y百分比
     */
    public void moveTo(float percentX, float percentY) {
        moveTo((int) (percentX * width), (int) (percentY * height));
    }

    /**
     * 按下 https://github.com/openstf/minitouch#d-contact-x-y-pressure
     *
     * @param x
     * @param y
     */
    public void touchDown(int x, int y) {
        commit(String.format("d 0 %s %s 50", x, y));
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
    public void moveTo(int x, int y) {
        commit(String.format("m 0 %d %d 50", x, y));
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
