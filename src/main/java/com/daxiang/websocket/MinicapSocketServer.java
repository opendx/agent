package com.daxiang.websocket;

import com.daxiang.App;
import com.daxiang.android.AndroidDevice;
import com.daxiang.android.AndroidDeviceHolder;
import com.daxiang.android.stf.Minicap;
import com.daxiang.api.MasterApi;
import com.daxiang.model.Device;
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
@ServerEndpoint(value = "/minicap/{deviceId}/{username}")
public class MinicapSocketServer {

    private static Map<String, Session> sessionPool = new ConcurrentHashMap<>();

    private Minicap minicap;
    private AndroidDevice androidDevice;
    private String deviceId;
    private Thread handleImgDataThread;

    @OnOpen
    public void onOpen(@PathParam("deviceId") String deviceId, @PathParam("username") String username, Session session) throws Exception {
        log.info("[{}][minicap][socketserver]onOpen: username -> {}", deviceId, username);
        this.deviceId = deviceId;

        // todo 这里使用getAsyncRemote可以解决卡顿
        RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
        basicRemote.sendText("minicap websocket连接成功");

        androidDevice = AndroidDeviceHolder.get(deviceId);

        if (androidDevice == null || !androidDevice.isConnected()) {
            basicRemote.sendText(deviceId + "手机未连接");
            session.close();
            return;
        }

        if (androidDevice.getDevice().getStatus() != Device.IDLE_STATUS) {
            basicRemote.sendText(deviceId + "设备未处于闲置状态，" + androidDevice.getDevice().getUsername() + "使用中");
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

        basicRemote.sendText("启动minicap服务...");
        minicap = androidDevice.getMinicap();
        minicap.start(minicap.convertVirtualResolution(Integer.parseInt(App.getProperty("displayWidth"))), 0);
        basicRemote.sendText("启动minicap服务完成");

        handleImgDataThread = new Thread(() -> {
            BlockingQueue<byte[]> imgQueue = minicap.getImgQueue();
            while (true) {
                byte[] img;
                try {
                    img = imgQueue.take();
                } catch (InterruptedException e) {
                    log.info("[{}][minicap][socketserver]停止从imgQueue获取数据", deviceId);
                    break;
                }
                try {
                    basicRemote.sendBinary(ByteBuffer.wrap(img));
                } catch (IOException e) {
                    log.error("[{}][minicap][socketserver]发送图片数据出错", deviceId, e);
                }
            }
            log.info("[{}][minicap][socketserver]停止发送图片数据", deviceId);
        }, "MinicapSocketServer-ImageDataTakerAndSender-" + deviceId);
        handleImgDataThread.start();

        Device device = androidDevice.getDevice();
        device.setStatus(Device.USING_STATUS);
        device.setUsername(username);
        MasterApi.getInstance().saveDevice(device);
        log.info("[{}][minicap][socketserver]数据库状态改为{}使用中", deviceId, username);
    }

    @OnClose
    public void onClose() {
        log.info("[{}][minicap][socketserver]onClose", deviceId);
        sessionPool.remove(deviceId);

        if (handleImgDataThread != null) {
            handleImgDataThread.interrupt();
        }

        if (minicap != null && androidDevice != null) {
            minicap.stop();
            Device device = androidDevice.getDevice();
            // 因为手机可能被拔出离线，AndroidDeviceChangeService.deviceDisconnected已经在数据库改为离线，这里不能改为闲置
            if (device != null && device.getStatus() == Device.USING_STATUS) {
                device.setStatus(Device.IDLE_STATUS);
                MasterApi.getInstance().saveDevice(device);
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