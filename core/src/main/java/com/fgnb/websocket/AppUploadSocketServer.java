package com.fgnb.websocket;

import com.android.ddmlib.InstallException;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.enums.DeviceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
//虽然@Component默认是单例模式的，但springboot还是会为每个websocket连接初始化一个bean
@Component
@ServerEndpoint("/upload/{deviceId}")
public class AppUploadSocketServer {


    private Session session;
    private String deviceId;
    private boolean isInstalling = false;
    File apkFile;

    @OnOpen
    public void onOpen(@PathParam("deviceId") String deviceId, Session session){
        this.deviceId = deviceId;
        this.session = session;
        log.info("session open "+session.getId());
    }

    @OnClose
    public void onClose(Session session){
        //删除apk文件
        if(apkFile!=null && apkFile.exists()){
            apkFile.delete();
        }
        log.info("session close "+session.getId());
    }

    @OnError
    public void onError(Throwable throwable){
        throwable.printStackTrace();
        log.error("session error");
    }

    @OnMessage
    public void onMessage(String message){
        log.info("on message=>"+ message);
    }

    private void closeSession(){
        if(session!=null && session.isOpen()){
            try {
                session.close();
            } catch (IOException e) {
                log.error("关闭session出错");
            }
        }
    }

    //最大200M
    @OnMessage(maxMessageSize = 209715200L)
    public void onMessage(Session session,byte[] message) throws Exception{
        if(isInstalling){
            WebSocketUtil.sendText(session,"正在安装，请稍后重试");
        }

        //检测手机是否连接
        AndroidDevice androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        if(androidDevice == null || !androidDevice.isConnected()){
            log.info("[{}]设备未连接",deviceId);
            WebSocketUtil.sendText(session,deviceId+"手机未连接");
            return;
        }

        isInstalling = true;
        WebSocketUtil.sendText(session,"服务端接收apk完成，开始安装");

        //获取apk文件
        String apkPath = System.currentTimeMillis()+".apk";
        apkFile = new File(apkPath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(apkFile);
            fos.write(message);
        } catch (Exception e) {
            log.error("写入apk文件出错",e);
            isInstalling = false;
            WebSocketUtil.sendText(session,"服务端写入apk文件出错，请稍后再试");
            closeSession();
            return;
        }finally {
            if(fos!=null){
                try{
                    fos.flush();
                    fos.close();
                }catch (Exception e){
                    log.error("关闭输出流出错",e);
                }
            }
        }
        //安装app
        try {
            androidDevice.getIDevice().installPackage(apkPath,true,null);
        } catch (InstallException e) {
            log.error("安装失败",e);
            isInstalling = false;
            WebSocketUtil.sendText(session,"安装apk失败，请稍后重试");
            closeSession();
            return;
        }

        isInstalling = false;
        WebSocketUtil.sendText(session,"安装完成");
        closeSession();
    }


}
