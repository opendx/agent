package com.fgnb.websocket;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.stf.minicap.Minicap;
import com.fgnb.api.ServerApi;
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
 * minicap
 */
@Slf4j
@Component //虽然@Component默认是单例模式的，但springboot还是会为每个websocket连接初始化一个bean
@ServerEndpoint(value = "/minicap/{deviceId}/{userName}")
public class MinicapSocketServer {

    public static Map<String, Session> minicapSessionMap = new ConcurrentHashMap<>();

    private String deviceId;
    private Session session;
    private Minicap minicap;

    private ServerApi uiServerApi = App.getBean(ServerApi.class);

    @OnOpen
    public void onOpen(@PathParam("deviceId") String deviceId, @PathParam("userName") String userName, Session session) throws Exception {

        this.session = session;
        this.deviceId = deviceId;

        log.info("[{}]已连接MinicapSocketServer，sessionid => {}", deviceId, session.getId());

        WebSocketUtil.sendText(session, "minicap websocket连接成功");

        //检测手机是否连接
        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if (androidDevice == null || !androidDevice.isConnected()) {
            log.info("[{}]设备未连接", deviceId);
            WebSocketUtil.sendText(session, deviceId + "手机未连接,请稍后重试");
            return;
        }
        //检测手机是否闲置
        if (androidDevice.getDevice().getStatus() != Device.IDLE_STATUS) {
            log.info("[{}]设备未处于闲置状态", deviceId);
            WebSocketUtil.sendText(session, deviceId + "设备未处于闲置状态," + androidDevice.getDevice().getUsername() + "使用中");
            return;
        }

        //如果有其他连接在使用该手机，则不往下处理并提示用户该手机在被占用
        Session otherSession = minicapSessionMap.get(deviceId);
        if (otherSession != null && otherSession.isOpen()) {
            log.info("[{}]检查到有其他连接正在使用minicap服务,不做任何处理，当前sessionid => {}，正在使用minicap的连接sessionid => {}", deviceId, session.getId(), otherSession.getId());
            WebSocketUtil.sendText(session, deviceId + "手机正在被" + otherSession.getId() + "连接占用,请稍后重试");
            return;
        }

        //将设备id和session放入map
        minicapSessionMap.put(deviceId, session);

        WebSocketUtil.sendText(session, "开始启动手机minicap服务");

        minicap = new Minicap(androidDevice);
        minicap.start("408x720", 0);

        WebSocketUtil.sendText(session, "minicap图片数据处理中," + "端口为:1" + ",session id:" + session.getId());

        //minicap连接成功 则把手机改为使用中
        Device device = AndroidDeviceHolder.get(deviceId).getDevice();
        device.setStatus(Device.USING_STATUS);
        device.setUsername(userName);
        uiServerApi.saveDevice(device);
        log.info("[{}]数据库状态改为使用中", deviceId);

        //3.将图片队列里的数据发送给浏览器
        BlockingQueue<byte[]> imgQueue = minicap.getImgQueue();
        while (minicap.isParseFrame()) {
            byte[] img = imgQueue.take();
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(img));
        }
    }

    /**
     * socket关闭：minicap资源回收，设备数据库改为闲置
     * 如果设备已经被其他连接使用，则minicapDataHandler为null 不会释放掉手机的minicap资源
     */
    @OnClose
    public void onClose() {
        log.info("[{}]断开MinicapSocketServer连接，sessionid => {}", deviceId, session.getId());
        //由于可能有多个连接访问进来，只允许一个连接使用，意味着只有minicapDataHandler被成功初始化的连接才是占用的连接，占用连接断开才做资源回收操作
        if (minicap != null) {
            //断开连接后 移除map里的session
            minicapSessionMap.remove(deviceId);
            //停止minicap
            minicap.stop();
            //minicap总是比minitouch/adbkit等晚很多关闭，所以只要minicap websocket关闭 则意味着手机可以变为闲置了
            Device device = AndroidDeviceHolder.get(deviceId).getDevice();
            //因为手机可能被拔出离线，AndroidDeviceChangeService.deviceDisconnected已经在数据库改为离线，这里不能改为闲置
            if (device != null && device.getStatus() == Device.USING_STATUS) {
                //将设备改为闲置
                device.setStatus(Device.IDLE_STATUS);
                uiServerApi.saveDevice(device);
                log.info("[{}]数据库状态改为闲置", deviceId);
            }
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[{}]minicap websocket onError，sessionid => {}", deviceId, session.getId(), t);
    }


    @OnMessage
    public void onMessage(String message) {
        log.info("来自客户端的消息:{} {}", message, session.getId());
    }
}