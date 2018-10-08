package com.fgnb.android.stf;

import com.fgnb.websocket.MinitouchSocketServer;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.io.IOException;

/**
 * Created by jiangyitao.
 * stf资源回收器
 * 用于回收minicap minitouch adbkit等资源
 */
@Slf4j
public class StfResourceReleaser {

    private String deviceId;

    public StfResourceReleaser(String deviceId) {
        this.deviceId = deviceId;
    }

    public void release(){
        //断开minitouch session。断开minitouch websocket时会把其他资源统统回收
        Session minitouchSession = MinitouchSocketServer.minitouchSessionMap.get(deviceId);
        if(minitouchSession!=null && minitouchSession.isOpen()){
            try {
                minitouchSession.close();
                log.info("[{}]设备断开，minitouchSession closed",deviceId);
            } catch (IOException e) {
                log.error("[{}]minitouchSession close error",deviceId,e);
            }
        }
    }
}
