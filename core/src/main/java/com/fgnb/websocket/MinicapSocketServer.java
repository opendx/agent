package com.fgnb.websocket;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.stf.minicap.Minicap;
import com.fgnb.api.MasterApi;
import com.fgnb.model.Device;
import com.fgnb.App;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
@ServerEndpoint(value = "/minicap/{deviceId}/{username}")
public class MinicapSocketServer {

    private static Map<String, Session> sessionPool = new ConcurrentHashMap<>();

    private Minicap minicap;
    private String deviceId;

    @OnOpen
    public void onOpen(@PathParam("deviceId") String deviceId, @PathParam("username") String username, Session session) throws Exception {
        log.info("[{}][minicap][socketserver]onOpen: username -> {}", deviceId, username);
        this.deviceId = deviceId;

        RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
        basicRemote.sendText("minicap websocket连接成功");

        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if (androidDevice == null || !androidDevice.isConnected()) {
            basicRemote.sendText(deviceId + "手机未连接");
            return;
        }

        if (androidDevice.getDevice().getStatus() != Device.IDLE_STATUS) {
            basicRemote.sendText(deviceId + "设备未处于闲置状态，" + androidDevice.getDevice().getUsername() + "使用中");
            return;
        }

        Session otherSession = sessionPool.get(deviceId);
        if (otherSession != null && otherSession.isOpen()) {
            basicRemote.sendText(deviceId + "手机正在被" + otherSession.getId() + "连接占用，请稍后重试");
            return;
        }

        sessionPool.put(deviceId, session);

        basicRemote.sendText("启动minicap服务...");
        minicap = new Minicap(androidDevice);
        minicap.start("408x720", 0);
        basicRemote.sendText("启动minicap服务完成");

        Device device = androidDevice.getDevice();
        device.setStatus(Device.USING_STATUS);
        device.setUsername(username);
        App.getBean(MasterApi.class).saveDevice(device);
        log.info("[{}][minicap][socketserver]数据库状态改为{}使用中", deviceId, username);

        BlockingQueue<byte[]> imgQueue = minicap.getImgQueue();
        new Thread(() -> {
            while (minicap.isParseFrame()) {
                try {
                    byte[] img = imgQueue.take();
                    if (session.isOpen()) {
                        basicRemote.sendBinary(ByteBuffer.wrap(img));
                    }
                } catch (Exception e) {
                    log.error("[{}][minicap][socketserver]图片处理出错", e);
                }
            }
            log.info("[{}][minicap][socketserver]停止发送图片数据", deviceId);
        }).start();
    }

    @OnClose
    public void onClose() {
        log.info("[{}][minicap][socketserver]onClose", deviceId);
        if (minicap != null) {
            sessionPool.remove(deviceId);
            minicap.stop();

            Device device = AndroidDeviceHolder.get(deviceId).getDevice();
            //因为手机可能被拔出离线，AndroidDeviceChangeService.deviceDisconnected已经在数据库改为离线，这里不能改为闲置
            if (device != null && device.getStatus() == Device.USING_STATUS) {
                device.setStatus(Device.IDLE_STATUS);
                App.getBean(MasterApi.class).saveDevice(device);
                log.info("[{}][minicap][socketserver]数据库状态改为闲置", deviceId);
            }
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[{}][minicap][socketserver]onError", deviceId, t);
    }


    @OnMessage
    public void onMessage(String message) {
        log.info("[{}][minicap][socketserver]onMessage: {}", deviceId, message);
    }
}