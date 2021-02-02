package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.mobile.android.AndroidDevice;
import com.daxiang.core.mobile.android.scrcpy.Scrcpy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
@ServerEndpoint(value = "/scrcpy/android/{mobileId}/user/{username}/project/{projectId}")
public class AndroidScrcpyWsServer extends DeviceWsServer {

    private Scrcpy scrcpy;

    @OnOpen
    public void onOpen(@PathParam("mobileId") String mobileId, @PathParam("username") String username,
                       @PathParam("projectId") Integer projectId, Session session) throws Exception {
        onWebsocketOpenStart(mobileId, username, session);
        scrcpy = ((AndroidDevice) device).getScrcpy();

        sender.sendText("启动scrcpy...");
        scrcpy.start(imgData -> {
            try {
                sender.sendBinary(imgData);
            } catch (IOException e) {
                log.error("[{}]发送scrcpy数据异常", mobileId, e);
            }
        });

        freshDriver(projectId);
        onWebsocketOpenFinish();
    }

    @OnClose
    public void onClose() {
        if (scrcpy != null) {
            scrcpy.stop();
            onWebSocketClose();
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[{}]onError", deviceId, t);
    }

    @OnMessage
    public void onMessage(String msg) {
        JSONObject message = JSON.parseObject(msg);
        String operation = message.getString("operation");
        switch (operation) {
            case "m":
                scrcpy.moveTo(message.getIntValue("x"), message.getIntValue("y"), message.getIntValue("width"), message.getIntValue("height"));
                break;
            case "d":
                scrcpy.touchDown(message.getIntValue("x"), message.getIntValue("y"), message.getIntValue("width"), message.getIntValue("height"));
                break;
            case "u":
                scrcpy.touchUp(message.getIntValue("x"), message.getIntValue("y"), message.getIntValue("width"), message.getIntValue("height"));
                break;
            case "k":
                int keycode = message.getIntValue("keycode");
                int metaState = message.getIntValue("metaState");
                scrcpy.keyDown(keycode, metaState);
                scrcpy.keyUp(keycode, metaState);
                break;
            case "kd":
                scrcpy.keyDown(message.getIntValue("keycode"), message.getIntValue("metaState"));
                break;
            case "ku":
                scrcpy.keyUp(message.getIntValue("keycode"), message.getIntValue("metaState"));
                break;
        }
    }

}
