package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.api.MasterApi;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.ios.IosDevice;
import com.daxiang.core.ios.IosUtil;
import com.daxiang.model.Device;
import com.daxiang.model.Response;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
@ServerEndpoint(value = "/ios/{deviceId}/{username}")
public class IosSocketServer {

    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    private IosDevice iosDevice;
    private String deviceId;

    PointOption downPointOption;
    PointOption moveToPointOption;

    long pressStartTime;

    @OnOpen
    public void onOpen(@PathParam("deviceId") String deviceId, @PathParam("username") String username, Session session) throws Exception {
        log.info("[ios-websocket][{}]onOpen: username -> {}", deviceId, username);
        this.deviceId = deviceId;

        RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
        basicRemote.sendText("ios websocket连接成功");

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
        iosDevice = (IosDevice) mobileDevice;

        Device device = iosDevice.getDevice();
        device.setStatus(Device.USING_STATUS);
        device.setUsername(username);
        MasterApi.getInstance().saveDevice(device);
        log.info("[ios-websocket][{}]数据库状态改为{}使用中", deviceId, username);

        Response response = App.getBean(MobileService.class).freshDriver(deviceId);
        basicRemote.sendText(JSON.toJSONString(response));

        // 转发本地端口到wdaMjpegServer,这样可以通过localhost访问到wdaMjpegServer获取屏幕数据
        iosDevice.startMjpegServerIproxy();

        Thread.sleep(1000);
        // 前端拿到ok，可以渲染出<img src="xx">显示屏幕数据
        basicRemote.sendText("ok");
    }

    @OnClose
    public void onClose() {
        log.info("[ios-websocket][{}]onClose", deviceId);

        if (iosDevice != null) {
            SESSION_POOL.remove(deviceId);
            iosDevice.stopMjpegServerIproxy();

            Device device = iosDevice.getDevice();
            // 因为手机可能被拔出离线,DefaultIosDeviceChangeListener.iosDeviceDisconnected已经在数据库改为离线，这里不能改为闲置
            if (device != null && device.getStatus() == Device.USING_STATUS) {
                device.setStatus(Device.IDLE_STATUS);
                MasterApi.getInstance().saveDevice(device);
                log.info("[ios-websocket][{}]数据库状态改为闲置", deviceId);
            }
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
    private synchronized void handleMessage(String msg) {
        JSONObject jsonObject = JSON.parseObject(msg);
        String operation = jsonObject.getString("operation");
        switch (operation) {
            case "m":
                moveToPointOption = iosDevice.getPointOption(jsonObject.getFloat("percentOfX"), jsonObject.getFloat("percentOfY"));
                break;
            case "d":
                downPointOption = iosDevice.getPointOption(jsonObject.getFloat("percentOfX"), jsonObject.getFloat("percentOfY"));
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
}