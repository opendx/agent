package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.android.scrcpy.Scrcpy;
import com.daxiang.service.MobileService;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.android.AndroidDriver;
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
@ServerEndpoint(value = "/scrcpy/android/{deviceId}/user/{username}/platform/{platform}")
public class AndroidScrcpySocketServer {

    private MobileService mobileService;

    private AndroidDevice androidDevice;
    private AndroidDriver androidDriver;
    private String deviceId;
    private Scrcpy scrcpy;

    @OnOpen
    public void onOpen(@PathParam("deviceId") String deviceId, @PathParam("username") String username, @PathParam("platform") Integer platform, Session session) throws Exception {
        log.info("[android-scrcpy-websocket][{}]onOpen: username -> {}", deviceId, username);
        this.deviceId = deviceId;

        RemoteEndpoint.Basic remoteEndpoint = session.getBasicRemote();
        remoteEndpoint.sendText("android scrcpy websocket连接成功");

        MobileDevice mobileDevice = MobileDeviceHolder.getIdleDevice(deviceId);
        if (mobileDevice == null) {
            remoteEndpoint.sendText("设备未处于闲置状态，无法使用");
            session.close();
            return;
        }

        Session openedSession = MobileDeviceWebSocketSessionPool.getOpenedSession(deviceId);
        if (openedSession != null) {
            remoteEndpoint.sendText(deviceId + "正在被" + openedSession.getId() + "连接占用，请稍后重试");
            session.close();
            return;
        }

        MobileDeviceWebSocketSessionPool.put(deviceId, session);

        androidDevice = (AndroidDevice) mobileDevice;
        androidDevice.getDevice().setUsername(username);

        mobileService = App.getBean(MobileService.class);
        mobileService.saveUsingDeviceToServer(androidDevice);

        scrcpy = androidDevice.getScrcpy();
        scrcpy.start(imgData -> {
            try {
                remoteEndpoint.sendBinary(imgData);
            } catch (IOException e) {
                log.error("[android-scrcpy-websocket][{}]发送scrcpy数据异常", deviceId, e);
            }
        });

        remoteEndpoint.sendText("初始化appium driver...");
        androidDriver = (AndroidDriver) androidDevice.freshAppiumDriver(platform);
        remoteEndpoint.sendText("初始化appium driver完成");

        remoteEndpoint.sendText(JSON.toJSONString(ImmutableMap.of("appiumSessionId", androidDriver.getSessionId().toString())));
    }

    @OnClose
    public void onClose() {
        log.info("[android-scrcpy-websocket][{}]onClose", deviceId);

        if (androidDevice != null) {
            MobileDeviceWebSocketSessionPool.remove(deviceId);
            scrcpy.stop();
            androidDevice.quitAppiumDriver();
            mobileService.saveIdleDeviceToServer(androidDevice);
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[android-scrcpy-websocket][{}]onError", deviceId, t);
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
                scrcpy.moveTo(message.getInteger("x"), message.getInteger("y"), message.getInteger("width"), message.getInteger("height"));
                break;
            case "d":
                scrcpy.touchDown(message.getInteger("x"), message.getInteger("y"), message.getInteger("width"), message.getInteger("height"));
                break;
            case "u":
                scrcpy.touchUp(message.getInteger("x"), message.getInteger("y"), message.getInteger("width"), message.getInteger("height"));
                break;
            case "home":
                scrcpy.home();
                break;
            case "back":
                scrcpy.back();
                break;
            case "power":
                scrcpy.power();
                break;
            case "menu":
                scrcpy.menu();
                break;
        }
    }
}