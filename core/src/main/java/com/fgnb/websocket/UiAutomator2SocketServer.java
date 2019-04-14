package com.fgnb.websocket;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.uiautomator.UiautomatorServerManager;
import com.fgnb.model.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
@ServerEndpoint("/uiautomator2server/{deviceId}")
public class UiAutomator2SocketServer {


    public static Map<String,Session> uiautomator2SessionMap = new ConcurrentHashMap<>();
    public static Map<String,UiautomatorServerManager> uiautomatorServerManagerMap = new ConcurrentHashMap<>();

    private String deviceId;
    private Session session;
    private UiautomatorServerManager uiautomatorServerManager;

    @OnOpen
    public void onOpen(@PathParam("deviceId")String deviceId, Session session) throws Exception {
        this.deviceId = deviceId;
        this.session = session;

        log.info("[{}]已连接UiAutomator2SocketServer，sessionid => {}",deviceId,session.getId());

        WebSocketUtil.sendText(session,"UiAutomator2SocketServer连接成功");

        //检测手机是否连接
        AndroidDevice androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        if(androidDevice == null || !androidDevice.isConnected()){
            log.info("[{}]设备未连接",deviceId);
            WebSocketUtil.sendText(session,deviceId+"手机未连接");
            return;
        }
        //检测手机是否闲置
        if(androidDevice.getDevice().getStatus() != Device.IDLE_STATUS){
            log.info("[{}]设备未处于闲置状态",deviceId);
            WebSocketUtil.sendText(session,deviceId+"设备未处于闲置状态");
            return;
        }

        //如果有其他连接在使用该手机，则不往下处理并提示用户该手机在被占用
        Session otherSession = uiautomator2SessionMap.get(deviceId);
        if(otherSession!=null && otherSession.isOpen()){
            log.info("[{}]检查到有其他连接正在使用uiautomator2server服务,不做任何处理，当前sessionid => {}，正在使用uiautomator2server的连接sessionid => {}",deviceId,session.getId(),otherSession.getId());
            WebSocketUtil.sendText(session,deviceId+"手机正在被"+otherSession.getId()+"连接占用,请稍后重试");
            return;
        }

        uiautomator2SessionMap.put(deviceId,session);

        WebSocketUtil.sendText(session,"开始启动手机uiautomator2 server");

        uiautomatorServerManager = new UiautomatorServerManager(androidDevice);
        uiautomatorServerManager.startServer();
        uiautomatorServerManager.createForward();

        uiautomatorServerManagerMap.put(deviceId,uiautomatorServerManager);

        WebSocketUtil.sendText(session,"开启uiautomator2 server成功,端口为:"+uiautomatorServerManager.getPort()+",sessionid:"+session.getId());

    }

    @OnClose
    public void onClose(Session session){
        log.info("[{}]断开UiAutomator2SocketServer连接，sessionid => {}",deviceId,session.getId());
        //由于可能有多个连接访问进来，只允许一个连接使用，意味着只有uiautomatorServerManager被成功初始化的连接才是占用的连接，占用连接断开才做资源回收操作
        if(uiautomatorServerManager!=null){
            uiautomator2SessionMap.remove(deviceId);
            uiautomatorServerManagerMap.remove(deviceId);
            uiautomatorServerManager.stopServer();
        }
    }

}
