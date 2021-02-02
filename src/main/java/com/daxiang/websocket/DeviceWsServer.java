package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.daxiang.core.Device;
import com.daxiang.core.DeviceHolder;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.server.ServerClient;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class DeviceWsServer {

    protected String deviceId;
    protected RemoteEndpoint.Basic sender;
    protected Device device;

    protected void onWebsocketOpenStart(String deviceId, String username, Session session) throws IOException {
        log.info("[{}]ws on open, username: {}", deviceId, username);
        this.deviceId = deviceId;
        sender = session.getBasicRemote();

        Device mDevice = DeviceHolder.getIdleDevice(deviceId);

        if (mDevice == null) {
            String msg = "当前device未闲置";
            sender.sendText(msg);
            throw new IllegalStateException(msg);
        }

        Session openingSession = WebSocketSessionPool.getOpeningSession(deviceId);
        if (openingSession != null) {
            String msg = String.format("当前device正在被%s连接占用", openingSession.getId());
            sender.sendText(msg);
            throw new IllegalStateException(msg);
        }

        WebSocketSessionPool.put(deviceId, session);
        mDevice.usingToServer(username);
        device = mDevice;
    }

    protected void freshDriver(Integer projectId) throws IOException {
        Capabilities projectCaps = ServerClient.getInstance().getCapabilitiesByProjectId(projectId);

        sender.sendText("初始化driver...");
        device.freshDriver(projectCaps, true);
        sender.sendText("初始化driver完成");
    }

    protected void onWebsocketOpenFinish() throws IOException {
        String sessionId = device.getDriver().getSessionId().toString();
        sender.sendText(JSON.toJSONString(ImmutableMap.of("driverSessionId", sessionId)));
    }

    protected void onWebSocketClose() {
        log.info("[{}]ws on close", deviceId);
        WebSocketSessionPool.remove(deviceId);
        if (device.isConnected()) {
            if (device instanceof MobileDevice) {
                ((MobileDevice) device).stopLogsBroadcast();
            }
            device.quitDriver();
            device.idleToServer();
        } else {
            log.info("[{}]device未连接", deviceId);
        }
    }

}
