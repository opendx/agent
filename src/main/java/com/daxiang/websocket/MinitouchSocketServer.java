package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.stf.Minitouch;
import com.daxiang.model.Device;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 * minitouch
 */
@Slf4j
@Component
@ServerEndpoint(value = "/minitouch/{deviceId}")
public class MinitouchSocketServer {

    public static Map<String, Session> sessionPool = new ConcurrentHashMap<>();

    private Minitouch minitouch;
    private String deviceId;
    private MobileDevice mobileDevice;

    @OnOpen
    public void onOpen(@PathParam("deviceId") String deviceId, Session session) throws Exception {
        log.info("[{}][minitouch][socketserver]onOpen", deviceId);
        this.deviceId = deviceId;

        RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
        basicRemote.sendText("minitouch websocket连接成功");

        mobileDevice = MobileDeviceHolder.get(deviceId);
        if (mobileDevice == null || !mobileDevice.isConnected()) {
            basicRemote.sendText(deviceId + "手机未连接");
            session.close();
            return;
        }

        if (mobileDevice.getDevice().getStatus() != Device.IDLE_STATUS) {
            basicRemote.sendText(deviceId + "设备未处于闲置状态，" + mobileDevice.getDevice().getUsername() + "使用中");
            session.close();
            return;
        }

        Session otherSession = sessionPool.get(deviceId);
        if (otherSession != null && otherSession.isOpen()) {
            basicRemote.sendText(deviceId + "手机正在被" + otherSession.getId() + "连接占用，请稍后重试");
            session.close();
            return;
        }

        sessionPool.put(deviceId, session);

        basicRemote.sendText("启动minitouch服务...");
        minitouch = ((AndroidDevice) mobileDevice).getMinitouch();
        minitouch.start();
        basicRemote.sendText("启动minitouch服务完成");
    }

    @OnClose
    public void onClose() {
        log.info("[{}][minitouch][socketserver]onClose", deviceId);
        sessionPool.remove(deviceId);

        if (minitouch != null) {
            minitouch.stop();
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[{}][minitouch][socketserver]onError", deviceId, t);
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
        JSONObject jsonObject = JSON.parseObject(msg);
        String operation = jsonObject.getString("operation");
        switch (operation) {
            case "m":
                minitouch.moveTo(jsonObject.getFloat("percentOfX"), jsonObject.getFloat("percentOfY"));
                break;
            case "d":
                minitouch.touchDown(jsonObject.getFloat("percentOfX"), jsonObject.getFloat("percentOfY"));
                break;
            case "u":
                minitouch.touchUp();
                break;
            case "home":
                ((AndroidDriver) mobileDevice.getAppiumDriver()).pressKey(new KeyEvent(AndroidKey.HOME));
                break;
            case "back":
                ((AndroidDriver) mobileDevice.getAppiumDriver()).pressKey(new KeyEvent(AndroidKey.BACK));
                break;
            case "power":
                ((AndroidDriver) mobileDevice.getAppiumDriver()).pressKey(new KeyEvent(AndroidKey.POWER));
                break;
            case "menu":
                ((AndroidDriver) mobileDevice.getAppiumDriver()).pressKey(new KeyEvent(AndroidKey.MENU));
                break;
        }
    }
}