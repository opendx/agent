package com.fgnb.websocket;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.stf.adbkit.AdbKitManager;
import com.fgnb.android.stf.adbkit.AdbKitPortProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 * 远程调试
 */
@Slf4j
//虽然@Component默认是单例模式的，但springboot还是会为每个websocket连接初始化一个bean
@Component
@ServerEndpoint("/remotedebug/{deviceId}")
public class AdbKitSocketServer {


    public static Map<String,Session> adbKitSessionMap = new ConcurrentHashMap<>();

    private String deviceId;
    private Session session;
    private AdbKitManager adbKitManager;
    private int adbkitPort = -1;

    @OnOpen
    public void onOpen(@PathParam("deviceId")String deviceId, Session session) throws Exception{

        this.deviceId = deviceId;
        this.session = session;

        log.info("[{}]已连接AdbKitSocketServer，sessionid => {}",deviceId,session.getId());

        WebSocketUtil.sendText(session,"adbkit websocket连接成功");

        //检测手机是否连接
        AndroidDevice androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        if(androidDevice == null || !androidDevice.isConnected()){
            log.info("[{}]设备未连接",deviceId);
            WebSocketUtil.sendText(session,deviceId+"手机未连接");
            return;
        }

        //如果有其他连接在使用该手机，则不往下处理并提示用户该手机在被占用
        Session otherSession = adbKitSessionMap.get(deviceId);
        if(otherSession!=null && otherSession.isOpen()){
            log.info("[{}]检查到有其他连接正在使用adbkit服务,不做任何处理，当前sessionid => {}，正在使用adbkit的连接sessionid => {}",deviceId,session.getId(),otherSession.getId());
            WebSocketUtil.sendText(session,deviceId+"手机正在被"+otherSession.getId()+"连接占用,请稍后重试");
            return;
        }

        adbKitSessionMap.put(deviceId,session);
        WebSocketUtil.sendText(session,"开始启动手机远程调试服务");

        try {
            adbkitPort = AdbKitPortProvider.getAvailablePort();
            adbKitManager = new AdbKitManager(adbkitPort,deviceId);
        } catch (Exception e) {
            log.error("[{}]初始化AdbKitManager出错",deviceId,e);
            WebSocketUtil.sendText(session,"初始化AdbKitManager出错:"+e.getMessage());
            //关闭连接
            try {
                this.session.close();
            } catch (IOException e1) {
                log.error("[{}]关闭sessionid:{}出错",deviceId,session.getId());
            }
            return;
        }
        adbKitManager.startUsbDeviceToTcp();

        WebSocketUtil.sendText(session,"开启手机远程调试成功,端口为:"+adbkitPort+",sessionid:"+session.getId());
    }

    @OnClose
    public void onClose(Session session){

        log.info("[{}]断开AdbKitSocketServer连接，sessionid => {}",deviceId,session.getId());
        //由于可能有多个连接访问进来，只允许一个连接使用，意味着只有adbKitManager被成功初始化的连接才是占用的连接，占用连接断开才做资源回收操作
        if(adbKitManager!=null){
            //断开连接后 移除map里的session
            adbKitSessionMap.remove(deviceId);
            adbKitManager.stopUsbDeviceToTcp();
        }
    }

    @OnMessage
    public void onMessage(String msg){
        log.info("sessonId:{},onMessage:{}",session.getId(),msg);
        if("关闭webkit socket".equals(msg)){
            if(session!=null && session.isOpen()){
                try {
                    session.close();
                } catch (IOException e) {
                    log.error("关闭session出错",e);
                }
            }
        }
    }

}
