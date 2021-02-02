package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.core.mobile.android.AndroidDevice;
import io.appium.java_client.android.AndroidDriver;
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
@ServerEndpoint(value = "/stf/android/{mobileId}/user/{username}/project/{projectId}")
public class AndroidStfWsServer extends DeviceWsServer {

    private AndroidDevice androidDevice;

    @OnOpen
    public void onOpen(@PathParam("mobileId") String mobileId, @PathParam("username") String username,
                       @PathParam("projectId") Integer projectId, Session session) throws Exception {
        onWebsocketOpenStart(mobileId, username, session);
        androidDevice = (AndroidDevice) device;

        int width = androidDevice.getMobile().getScreenWidth();
        int height = androidDevice.getMobile().getScreenHeight();

        String realResolution = width + "x" + height;
        String virtualResolution = width / 2 + "x" + height / 2;

        // android5以下的Mobile不多，暂不处理横竖屏切换
        sender.sendText("启动minicap...");
        androidDevice.getMinicap().start(Integer.parseInt(App.getProperty("minicap-quality")),
                realResolution,
                virtualResolution,
                0,
                minicapImgData -> {
                    try {
                        sender.sendBinary(minicapImgData);
                    } catch (IOException e) {
                        log.error("[{}]发送minicap数据异常", mobileId, e);
                    }
                });

        sender.sendText("启动minitouch...");
        androidDevice.getMinitouch().start();

        freshDriver(projectId);
        onWebsocketOpenFinish();
    }

    @OnClose
    public void onClose() {
        if (androidDevice != null) {
            androidDevice.getMinitouch().stop();
            androidDevice.getMinicap().stop();
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
                androidDevice.getMinitouch()
                        .moveTo(message.getIntValue("x"), message.getIntValue("y"), message.getIntValue("width"), message.getIntValue("height"));
                break;
            case "d":
                androidDevice.getMinitouch()
                        .touchDown(message.getIntValue("x"), message.getIntValue("y"), message.getIntValue("width"), message.getIntValue("height"));
                break;
            case "u":
                androidDevice.getMinitouch().touchUp();
                break;
            case "k":
                ((AndroidDriver) androidDevice.getDriver()).pressKeyCode(message.getIntValue("keycode"));
                break;
        }
    }

}
