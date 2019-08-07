package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.api.MasterApi;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.android.stf.Minicap;
import com.daxiang.core.android.stf.Minitouch;
import com.daxiang.model.Device;
import com.daxiang.model.Response;
import com.daxiang.service.MobileService;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
@ServerEndpoint(value = "/android/{deviceId}/{username}")
public class AndroidSocketServer {

    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    private Minicap minicap;
    private Minitouch minitouch;
    private AndroidDevice androidDevice;
    private String deviceId;
    private Thread handleImgDataThread;

    @OnOpen
    public void onOpen(@PathParam("deviceId") String deviceId, @PathParam("username") String username, Session session) throws Exception {
        log.info("[android-websocket][{}]onOpen: username -> {}", deviceId, username);
        this.deviceId = deviceId;

        // todo 这里使用getAsyncRemote可以解决卡顿
        RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
        basicRemote.sendText("android websocket连接成功");

        MobileDevice mobileDevice = MobileDeviceHolder.getIdleDevice(deviceId);
        if (mobileDevice == null) {
            basicRemote.sendText("手机未处于闲置状态，无法使用");
            session.close();
            return;
        }

        Session otherSession = SESSION_POOL.get(deviceId);
        if (otherSession != null && otherSession.isOpen()) {
            basicRemote.sendText(deviceId + "手机正在被" + otherSession.getId() + "连接占用，请稍后重试");
            session.close();
            return;
        }

        SESSION_POOL.put(deviceId, session);
        androidDevice = (AndroidDevice) mobileDevice;

        Device device = mobileDevice.getDevice();
        device.setStatus(Device.USING_STATUS);
        device.setUsername(username);
        MasterApi.getInstance().saveDevice(device);
        log.info("[android-websocket][{}]数据库状态改为{}使用中", deviceId, username);

        basicRemote.sendText("启动minicap服务...");
        minicap = androidDevice.getMinicap();
        minicap.start(Integer.parseInt(App.getProperty("minicap-quality")), minicap.convertVirtualResolution(Integer.parseInt(App.getProperty("displayWidth"))), 0);
        basicRemote.sendText("启动minicap服务完成");

        handleImgDataThread = new Thread(() -> {
            BlockingQueue<byte[]> imgQueue = minicap.getImgQueue();
            while (true) {
                byte[] img;
                try {
                    img = imgQueue.take();
                } catch (InterruptedException e) {
                    log.info("[android-websocket][minicap][{}]停止从imgQueue获取数据", deviceId);
                    break;
                }
                try {
                    basicRemote.sendBinary(ByteBuffer.wrap(img));
                } catch (IOException e) {
                    log.error("[android-websocket][minicap][{}]发送图片数据出错", deviceId, e);
                }
            }
            log.info("[android-websocket][minicap][{}]停止发送图片数据", deviceId);
        }, "AndroidWebSocket-ImageDataHandler-" + deviceId);
        handleImgDataThread.start();

        basicRemote.sendText("启动minitouch服务...");
        minitouch = androidDevice.getMinitouch();
        minitouch.start();
        basicRemote.sendText("启动minitouch服务完成");

        Response response = App.getBean(MobileService.class).freshDriver(deviceId);
        basicRemote.sendText(JSON.toJSONString(response));
    }

    @OnClose
    public void onClose() {
        log.info("[android-websocket][{}]onClose", deviceId);

        if (handleImgDataThread != null) {
            SESSION_POOL.remove(deviceId);
            handleImgDataThread.interrupt();
        }

        if (minitouch != null) {
            minitouch.stop();
        }

        if (minicap != null) {
            minicap.stop();
        }

        if (androidDevice != null) {
            Device device = androidDevice.getDevice();
            // 因为手机可能被拔出离线，AndroidDeviceChangeListener.deviceDisconnected已经在数据库改为离线，这里不能改为闲置
            if (device != null && device.getStatus() == Device.USING_STATUS) {
                device.setStatus(Device.IDLE_STATUS);
                MasterApi.getInstance().saveDevice(device);
                log.info("[android-websocket][{}]数据库状态改为闲置", deviceId);
            }
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[android-websocket][{}]onError", deviceId, t);
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
                ((AndroidDriver) androidDevice.getAppiumDriver()).pressKey(new KeyEvent(AndroidKey.HOME));
                break;
            case "back":
                ((AndroidDriver) androidDevice.getAppiumDriver()).pressKey(new KeyEvent(AndroidKey.BACK));
                break;
            case "power":
                ((AndroidDriver) androidDevice.getAppiumDriver()).pressKey(new KeyEvent(AndroidKey.POWER));
                break;
            case "menu":
                ((AndroidDriver) androidDevice.getAppiumDriver()).pressKey(new KeyEvent(AndroidKey.MENU));
                break;
        }
    }

}