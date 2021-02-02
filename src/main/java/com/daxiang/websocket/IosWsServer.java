package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.mobile.ios.IosDevice;
import com.daxiang.core.mobile.ios.IosUtil;
import com.daxiang.core.mobile.ios.WdaMjpegInputStream;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
@ServerEndpoint(value = "/ios/{mobileId}/user/{username}/project/{projectId}")
public class IosWsServer extends DeviceWsServer {

    private IosDevice iosDevice;
    private int mobileWidth;
    private int mobileHeigth;

    private PointOption downPointOption;
    private PointOption moveToPointOption;
    private long pressStartTime;

    @OnOpen
    public void onOpen(@PathParam("mobileId") String mobileId, @PathParam("username") String username,
                       @PathParam("projectId") Integer projectId, Session session) throws Exception {
        onWebsocketOpenStart(mobileId, username, session);

        iosDevice = (IosDevice) device;
        mobileWidth = iosDevice.getMobile().getScreenWidth();
        mobileHeigth = iosDevice.getMobile().getScreenHeight();

        freshDriver(projectId);

        // 转发本地端口到wdaMjpegServer,这样可以通过localhost访问到wdaMjpegServer屏幕数据
        long mjpegServerPort = iosDevice.startMjpegServerIproxy();
        String mjpegServerUrl = "http://localhost:" + mjpegServerPort;

        URL url = new URL(mjpegServerUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30000); // ms
        conn.setReadTimeout(30000);    // ms

        // 连接wdaMjpegServer
        sender.sendText("连接wdaMjpegServer...");
        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                conn.connect();
                break;
            } catch (Exception e) {
                Thread.sleep(1000);
            }
            if (System.currentTimeMillis() - startTime > 15000) {
                throw new RuntimeException(String.format("[%s]连接%s失败", mobileId, mjpegServerUrl));
            }
        }

        new Thread(() -> {
            try (InputStream in = conn.getInputStream();
                 WdaMjpegInputStream wdaIn = new WdaMjpegInputStream(in)) {
                while (true) {
                    sender.sendBinary(wdaIn.readImg());
                }
            } catch (Exception e) {
                log.info("[{}]{}", mobileId, e.getMessage());
            }
            log.info("[{}]停止发送图片数据", mobileId);
            conn.disconnect();
        }).start();

        onWebsocketOpenFinish();
    }

    @OnClose
    public void onClose() {
        if (iosDevice != null) {
            iosDevice.stopMjpegServerIproxy();
            onWebSocketClose();
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[{}]onError", deviceId, t);
    }

    @OnMessage
    public void onMessage(String msg) {
        JSONObject message = JSON.parseObject(msg);
        String operation = message.getString("operation");
        switch (operation) {
            case "m":
                moveToPointOption = createPointOption(message.getInteger("x"), message.getInteger("y"), message.getInteger("width"), message.getInteger("height"));
                break;
            case "d":
                downPointOption = createPointOption(message.getInteger("x"), message.getInteger("y"), message.getInteger("width"), message.getInteger("height"));
                moveToPointOption = null;
                pressStartTime = System.currentTimeMillis();
                break;
            case "u":
                long pressDurationInMs = System.currentTimeMillis() - pressStartTime;

                TouchAction touchAction = new TouchAction((AppiumDriver) iosDevice.getDriver());
                touchAction.press(downPointOption).waitAction(WaitOptions.waitOptions(Duration.ofMillis(pressDurationInMs)));
                if (moveToPointOption != null) { // 移动过
                    // 前端每移动一点距离都会调用"m"，导致moveTo非常多，wda执行很慢。目前的处理只保留移动最后的坐标，这样会导致只能画直线，后续考虑优化
                    touchAction.moveTo(moveToPointOption);
                }

                touchAction.release();
                touchAction.perform();
                break;
            case "home":
                IosUtil.pressHome(iosDevice.getDriver());
                break;
        }
    }

    private PointOption createPointOption(int x, int y, int screenWidth, int screenHeight) {
        x = (int) (((float) x) / screenWidth * mobileWidth);
        y = (int) (((float) y) / screenHeight * mobileHeigth);
        return PointOption.point(x, y);
    }
}
