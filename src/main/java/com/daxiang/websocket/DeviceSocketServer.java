package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.Device;
import com.daxiang.core.DeviceHolder;
import com.daxiang.server.ServerClient;
import com.google.common.collect.ImmutableMap;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class DeviceSocketServer {

    protected String deviceId;
    protected RemoteEndpoint.Basic sender;
    protected Device device;

    protected void onWebsocketOpenStart(String deviceId, String username, Session session) throws IOException {
        this.deviceId = deviceId;
        sender = session.getBasicRemote();

        Device mDevice = DeviceHolder.getIdleDevice(deviceId);

        if (mDevice == null) {
            String msg = "当前设备未闲置";
            sender.sendText(msg);
            throw new IllegalStateException(msg);
        }

        Session openingSession = WebSocketSessionPool.getOpeningSession(deviceId);
        if (openingSession != null) {
            String msg = String.format("当前设备正在被%s连接占用", openingSession.getId());
            sender.sendText(msg);
            throw new IllegalStateException(msg);
        }

        WebSocketSessionPool.put(deviceId, session);
        mDevice.usingToServer(username);
        device = mDevice;
    }

    protected void freshDriver(Integer projectId) throws IOException {
        JSONObject projectCaps = ServerClient.getInstance().getCapabilitiesByProjectId(projectId);

        sender.sendText("初始化driver...");
        device.freshDriver(projectCaps);
        sender.sendText("初始化driver完成");
    }

    protected void onWebsocketOpenFinish() throws IOException {
        String sessionId = device.getDriver().getSessionId().toString();
        sender.sendText(JSON.toJSONString(ImmutableMap.of("driverSessionId", sessionId)));
    }

    protected void onWebSocketClose() {
        WebSocketSessionPool.remove(deviceId);
        if (device.isConnected()) {
            device.quitDriver();
            device.idleToServer();
        }
    }

}
