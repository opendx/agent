package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.ios.IosDevice;
import com.daxiang.core.ios.IosUtil;
import com.daxiang.service.MobileService;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.time.Duration;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
@ServerEndpoint(value = "/ios/{deviceId}/user/{username}/platform/{platform}")
public class IosSocketServer {

    private MobileService mobileService;

    private IosDevice iosDevice;
    private String deviceId;

    PointOption downPointOption;
    PointOption moveToPointOption;

    long pressStartTime;

    @OnOpen
    public void onOpen(@PathParam("deviceId") String deviceId, @PathParam("username") String username, @PathParam("platform") Integer platform, Session session) throws Exception {
        log.info("[ios-websocket][{}]onOpen: username -> {}", deviceId, username);
        this.deviceId = deviceId;

        RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
        basicRemote.sendText("ios websocket连接成功");

        MobileDevice mobileDevice = MobileDeviceHolder.getIdleDevice(deviceId);
        if (mobileDevice == null) {
            basicRemote.sendText("设备未处于闲置状态，无法使用");
            session.close();
            return;
        }

        Session openedSession = MobileDeviceWebSocketSessionPool.getOpenedSession(deviceId);
        if (openedSession != null) {
            basicRemote.sendText(deviceId + "正在被" + openedSession.getId() + "连接占用，请稍后重试");
            session.close();
            return;
        }

        MobileDeviceWebSocketSessionPool.put(deviceId, session);

        iosDevice = (IosDevice) mobileDevice;
        iosDevice.getDevice().setUsername(username);

        mobileService = App.getBean(MobileService.class);
        mobileService.saveUsingDeviceToServer(iosDevice);

        JSONObject response = new JSONObject();

        basicRemote.sendText("初始化appium driver...");
        response.put("appiumSessionId", mobileDevice.freshAppiumDriver(platform).getSessionId().toString());
        response.put("mjpegServerPort", ((IosDevice) mobileDevice).getMjpegServerPort());
        basicRemote.sendText("初始化appium driver完成");

        // 转发本地端口到wdaMjpegServer,这样可以通过localhost访问到wdaMjpegServer获取屏幕数据
        iosDevice.startMjpegServerIproxy();

        basicRemote.sendText(JSON.toJSONString(response));
    }

    @OnClose
    public void onClose() {
        log.info("[ios-websocket][{}]onClose", deviceId);

        if (iosDevice != null) {
            MobileDeviceWebSocketSessionPool.remove(deviceId);
            iosDevice.stopMjpegServerIproxy();
            iosDevice.quitAppiumDriver();
            mobileService.saveIdleDeviceToServer(iosDevice);
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[ios-websocket][{}]onError", deviceId, t);
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
        int width = iosDevice.getDevice().getScreenWidth();
        int height = iosDevice.getDevice().getScreenHeight();

        x = (int) (((float) x) / screenWidth * width);
        y = (int) (((float) y) / screenHeight * height);

        return PointOption.point(x, y);
    }
}