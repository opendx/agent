package com.fgnb.android.uiautomator;

import com.android.ddmlib.*;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidUtils;
import com.fgnb.android.PortProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class Uiautomator2Server {

    private static final String PACKAGE_NAME = "com.macaca.android.testing";
    private static final String START_UIAUTOMATOR2_SERVER_CMD = "am instrument -w -e class 'com.macaca.android.testing.UIAutomatorWD' com.macaca.android.testing.test/android.support.test.runner.AndroidJUnitRunner";
    private static final int UIAUTOMATOR2_SERVER_RUN_IN_PHONE_PORT = 9001;

    private AndroidDevice androidDevice;
    private String deviceId;
    private int localPort;

    public Uiautomator2Server(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
        this.deviceId = androidDevice.getId();
    }

    /**
     * 开启服务
     */
    public void start() throws Exception {
        try {
            boolean appRunning = AndroidUtils.isAppRunning(androidDevice.getIDevice(), PACKAGE_NAME);
            log.info("[{}][uiautomator2server]APP是否在运行：", deviceId, appRunning);
            if (appRunning) {
                log.info("[{}][uiautomator2server]强制停止服务", deviceId);
                AndroidUtils.forceStopApp(androidDevice.getIDevice(), PACKAGE_NAME);
            }
        } catch (Exception e) {
            log.error("[{}][uiautomator2server]before start error", deviceId, e);
        }

        localPort = PortProvider.getUiautomator2ServerPort();
        log.info("[{}][uiautomator2server]申请本地端口：{}", deviceId, localPort);

        log.info("[{}][uiautomator2server]adb forward: {} ->{}", deviceId, localPort, UIAUTOMATOR2_SERVER_RUN_IN_PHONE_PORT);
        androidDevice.getIDevice().createForward(localPort, UIAUTOMATOR2_SERVER_RUN_IN_PHONE_PORT);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                log.info("[{}][uiautomator2server]启动：{}", deviceId, START_UIAUTOMATOR2_SERVER_CMD);
                androidDevice.getIDevice().executeShellCommand(START_UIAUTOMATOR2_SERVER_CMD, new MultiLineReceiver() {
                    @Override
                    public void processNewLines(String[] lines) {
                        for (String line : lines) {
                            log.info("[{}][uiautomator2server]手机控制台输出：{}", deviceId, line);
                            if (!StringUtils.isEmpty(line) && line.startsWith("UIAutomatorWD->http://localhost:9001<-UIAutomatorWD")) {
                                //uiautomator2server启动完成
                                countDownLatch.countDown();
                            }
                        }
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }
                }, 0, TimeUnit.SECONDS);

                log.info("[{}][uiautomator2server]已停止运行", deviceId);
            } catch (Exception e) {
                log.error("[{}][uiautomator2server]启动出错", deviceId, e);
            }

            //手机未连接 adb forward会自己移除
            if (androidDevice.isConnected()) {
                try {
                    log.info("[{}][uiautomator2server]移除adb forward: {} -> {}", deviceId, localPort, UIAUTOMATOR2_SERVER_RUN_IN_PHONE_PORT);
                    androidDevice.getIDevice().removeForward(localPort, UIAUTOMATOR2_SERVER_RUN_IN_PHONE_PORT);
                } catch (Exception e) {
                    log.error("[{}][uiautomator2server]移除adb forward出错", deviceId, e);
                }
            }

        }).start();

        countDownLatch.await();
        log.info("[{}][uiautomator2server]uiautomator2server启动完成", deviceId);
    }

    public void stop() {
        log.info("[{}][uiautomator2server]停止服务...", deviceId);
        try {
            AndroidUtils.forceStopApp(androidDevice.getIDevice(), PACKAGE_NAME);
        } catch (Exception e) {
            log.error("[{}][uiautomator2server]停止服务出错", deviceId, e);
        }
        log.info("[{}][uiautomator2server]服务停止完成", deviceId);
    }

}
