package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidDevice;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
@ServerEndpoint(value = "/stf/android/{deviceId}/user/{username}/platform/{platform}")
public class AndroidStfSocketServer {

    private AndroidDevice androidDevice;
    private AndroidDriver androidDriver;
    private String deviceId;

    @OnOpen
    public void onOpen(@PathParam("deviceId") String deviceId, @PathParam("username") String username, @PathParam("platform") Integer platform, Session session) throws Exception {
        log.info("[android-stf-websocket][{}]onOpen: username -> {}", deviceId, username);
        this.deviceId = deviceId;

        RemoteEndpoint.Basic remoteEndpoint = session.getBasicRemote();
        remoteEndpoint.sendText("android stf websocket连接成功");

        MobileDevice mobileDevice = MobileDeviceHolder.getIdleDevice(deviceId);
        if (mobileDevice == null) {
            remoteEndpoint.sendText("手机未处于闲置状态，无法使用");
            session.close();
            return;
        }

        Session openedSession = MobileDeviceWebSocketSessionPool.getOpenedSession(deviceId);
        if (openedSession != null) {
            remoteEndpoint.sendText(deviceId + "手机正在被" + openedSession.getId() + "连接占用，请稍后重试");
            session.close();
            return;
        }

        MobileDeviceWebSocketSessionPool.put(deviceId, session);
        androidDevice = (AndroidDevice) mobileDevice;

        androidDevice.saveUsingDeviceToMaster(username);

        int width = mobileDevice.getDevice().getScreenWidth();
        int height = mobileDevice.getDevice().getScreenHeight();
        String realResolution = width + "x" + height;
        String virtualResolution = width / 2 + "x" + height / 2;

        // android5以下的手机不多，暂不处理横竖屏切换
        remoteEndpoint.sendText("启动minicap...");
        androidDevice.getMinicap().start(Integer.parseInt(App.getProperty("minicap-quality")),
                realResolution,
                virtualResolution,
                0,
                minicapImgData -> {
                    try {
                        remoteEndpoint.sendBinary(minicapImgData);
                    } catch (IOException e) {
                        log.error("[android-stf-websocket][{}]发送minicap数据异常", deviceId, e);
                    }
                });
        remoteEndpoint.sendText("启动minicap完成");

        remoteEndpoint.sendText("启动minitouch...");
        androidDevice.getMinitouch().start();
        remoteEndpoint.sendText("启动minitouch完成");

        remoteEndpoint.sendText("初始化appium driver...");
        androidDriver = (AndroidDriver)androidDevice.freshAppiumDriver(platform);
        remoteEndpoint.sendText("初始化appium driver完成");
        remoteEndpoint.sendText(JSON.toJSONString(ImmutableMap.of("appiumSessionId", androidDriver.getSessionId().toString())));
    }

    @OnClose
    public void onClose() {
        log.info("[android-stf-websocket][{}]onClose", deviceId);

        if (androidDevice != null) {
            MobileDeviceWebSocketSessionPool.remove(deviceId);
            androidDevice.getMinitouch().stop();
            androidDevice.getMinicap().stop();
            androidDevice.quitAppiumDriver();
            androidDevice.saveIdleDeviceToMaster();
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[android-stf-websocket][{}]onError", deviceId, t);
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
                androidDevice.getMinitouch()
                        .moveTo(message.getInteger("x"), message.getInteger("y"), message.getInteger("width"), message.getInteger("height"));
                break;
            case "d":
                androidDevice.getMinitouch()
                        .touchDown(message.getInteger("x"), message.getInteger("y"), message.getInteger("width"), message.getInteger("height"));
                break;
            case "u":
                androidDevice.getMinitouch().touchUp();
                break;
            case "home":
                androidDriver.pressKey(new KeyEvent(AndroidKey.HOME));
                break;
            case "back":
                androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
                break;
            case "power":
                androidDriver.pressKey(new KeyEvent(AndroidKey.POWER));
                break;
            case "menu":
                androidDriver.pressKey(new KeyEvent(AndroidKey.MENU));
                break;
        }
    }
}