package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.mobile.ios.IosDevice;
import com.daxiang.core.mobile.ios.IosUtil;
import com.daxiang.core.mobile.ios.WdaMjpegInputStream;
import com.daxiang.server.ServerClient;
import com.daxiang.service.MobileService;
import com.google.common.collect.ImmutableMap;
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
public class IosSocketServer {

    private MobileService mobileService;

    private IosDevice iosDevice;
    private String mobileId;

    PointOption downPointOption;
    PointOption moveToPointOption;

    long pressStartTime;

    @OnOpen
    public void onOpen(@PathParam("mobileId") String mobileId, @PathParam("username") String username, @PathParam("projectId") Integer projectId, Session session) throws Exception {
        log.info("[ios-websocket][{}]onOpen: username -> {}", mobileId, username);
        this.mobileId = mobileId;

        RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
        basicRemote.sendText("ios websocket连接成功");

        MobileDevice mobileDevice = MobileDeviceHolder.getIdleDevice(mobileId);
        if (mobileDevice == null) {
            basicRemote.sendText("设备未处于闲置状态，无法使用");
            session.close();
            return;
        }

        Session openedSession = WebSocketSessionPool.getOpenedSession(mobileId);
        if (openedSession != null) {
            basicRemote.sendText(mobileId + "正在被" + openedSession.getId() + "连接占用，请稍后重试");
            session.close();
            return;
        }

        WebSocketSessionPool.put(mobileId, session);

        iosDevice = (IosDevice) mobileDevice;
        iosDevice.getMobile().setUsername(username);

        mobileService = App.getBean(MobileService.class);
        mobileService.saveUsingDeviceToServer(iosDevice);

        JSONObject caps = ServerClient.getInstance().getCapabilitiesByProjectId(projectId);

        basicRemote.sendText("初始化appium driver...");
        AppiumDriver driver = mobileDevice.freshAppiumDriver(caps);
        basicRemote.sendText("初始化appium driver完成");

        // 转发本地端口到wdaMjpegServer,这样可以通过localhost访问到wdaMjpegServer获取屏幕数据
        int mjpegServerPort = iosDevice.startMjpegServerIproxy();
        String mjpegServerUrl = "http://localhost:" + mjpegServerPort;

        URL url = new URL(mjpegServerUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(30000); // ms
        connection.setReadTimeout(30000); // ms

        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                connection.connect();
                break;
            } catch (Exception e) {
                Thread.sleep(1000);
            }
            if (System.currentTimeMillis() - startTime > 15000) {
                throw new RuntimeException(String.format("连接%s失败", mjpegServerUrl));
            }
        }

        new Thread(() -> {
            try (InputStream in = connection.getInputStream();
                 WdaMjpegInputStream wdaIn = new WdaMjpegInputStream(in)) {
                while (true) {
                    basicRemote.sendBinary(wdaIn.readImg());
                }
            } catch (Exception e) {
                log.info("[ios-websocket][{}]{}", mobileId, e.getMessage());
            }
            log.info("[ios-websocket][{}]停止发送图片数据", mobileId);
            connection.disconnect();
        }).start();

        basicRemote.sendText(JSON.toJSONString(ImmutableMap.of("driverSessionId", driver.getSessionId().toString())));
    }

    @OnClose
    public void onClose() {
        log.info("[ios-websocket][{}]onClose", mobileId);

        if (iosDevice != null) {
            WebSocketSessionPool.remove(mobileId);
            iosDevice.stopMjpegServerIproxy();
            iosDevice.quitAppiumDriver();
            mobileService.saveIdleDeviceToServer(iosDevice);
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[ios-websocket][{}]onError", mobileId, t);
    }

    @OnMessage
    public void onMessage(String message) {
        handleMessage(message);
    }

    /**
     * 根据前端传来的操作，执行相应操作
     *
     * @param msg
     */
    private void handleMessage(String msg) {
        JSONObject message = JSON.parseObject(msg);
        String operation = message.getString("operation");
        switch (operation) {
            case "m":
                moveToPointOption = getPointOption(message.getInteger("x"), message.getInteger("y"), message.getInteger("width"), message.getInteger("height"));
                break;
            case "d":
                downPointOption = getPointOption(message.getInteger("x"), message.getInteger("y"), message.getInteger("width"), message.getInteger("height"));
                moveToPointOption = null;
                pressStartTime = System.currentTimeMillis();
                break;
            case "u":
                long pressDurationInMs = System.currentTimeMillis() - pressStartTime;
                TouchAction touchAction = new TouchAction(iosDevice.getAppiumDriver());
                touchAction.press(downPointOption).waitAction(WaitOptions.waitOptions(Duration.ofMillis(pressDurationInMs)));
                if (moveToPointOption != null) { // 移动过
                    // 前端每移动一点距离都会调用"m"，导致moveTo非常多，wda执行很慢。目前的处理只保留移动最后的坐标，这样会导致只能画直线，后续考虑优化
                    touchAction.moveTo(moveToPointOption);
                }
                touchAction.release();
                touchAction.perform();
                break;
            case "home":
                IosUtil.pressHome(iosDevice.getAppiumDriver());
                break;
        }
    }

    private PointOption getPointOption(int x, int y, int screenWidth, int screenHeight) {
        int width = iosDevice.getMobile().getScreenWidth();
        int height = iosDevice.getMobile().getScreenHeight();

        x = (int) (((float) x) / screenWidth * width);
        y = (int) (((float) y) / screenHeight * height);

        return PointOption.point(x, y);
    }
}