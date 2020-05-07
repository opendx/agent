package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.mobile.android.AndroidDevice;
import com.daxiang.server.ServerClient;
import com.daxiang.service.MobileService;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
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
public class AndroidStfSocketServer {

    private MobileService mobileService;

    private AndroidDevice androidDevice;
    private AndroidDriver androidDriver;
    private String mobileId;

    @OnOpen
    public void onOpen(@PathParam("mobileId") String mobileId, @PathParam("username") String username, @PathParam("projectId") Integer projectId, Session session) throws Exception {
        log.info("[android-stf-websocket][{}]onOpen: username -> {}", mobileId, username);
        this.mobileId = mobileId;

        RemoteEndpoint.Basic remoteEndpoint = session.getBasicRemote();
        remoteEndpoint.sendText("android stf websocket连接成功");

        MobileDevice mobileDevice = MobileDeviceHolder.getIdleDevice(mobileId);
        if (mobileDevice == null) {
            remoteEndpoint.sendText("设备未处于闲置状态，无法使用");
            session.close();
            return;
        }

        Session openedSession = WebSocketSessionPool.getOpenedSession(mobileId);
        if (openedSession != null) {
            remoteEndpoint.sendText(mobileId + "正在被" + openedSession.getId() + "连接占用，请稍后重试");
            session.close();
            return;
        }

        WebSocketSessionPool.put(mobileId, session);

        androidDevice = (AndroidDevice) mobileDevice;
        androidDevice.getMobile().setUsername(username);

        mobileService = App.getBean(MobileService.class);
        mobileService.saveUsingDeviceToServer(androidDevice);

        int width = mobileDevice.getMobile().getScreenWidth();
        int height = mobileDevice.getMobile().getScreenHeight();
        String realResolution = width + "x" + height;
        String virtualResolution = width / 2 + "x" + height / 2;

        // android5以下的设备不多，暂不处理横竖屏切换
        remoteEndpoint.sendText("启动minicap...");
        androidDevice.getMinicap().start(Integer.parseInt(App.getProperty("minicap-quality")),
                realResolution,
                virtualResolution,
                0,
                minicapImgData -> {
                    try {
                        remoteEndpoint.sendBinary(minicapImgData);
                    } catch (IOException e) {
                        log.error("[android-stf-websocket][{}]发送minicap数据异常", mobileId, e);
                    }
                });
        remoteEndpoint.sendText("启动minicap完成");

        remoteEndpoint.sendText("启动minitouch...");
        androidDevice.getMinitouch().start();
        remoteEndpoint.sendText("启动minitouch完成");

        JSONObject caps = ServerClient.getInstance().getCapabilitiesByProjectId(projectId);

        remoteEndpoint.sendText("初始化appium driver...");
        androidDriver = (AndroidDriver)androidDevice.freshAppiumDriver(caps);
        remoteEndpoint.sendText("初始化appium driver完成");

        remoteEndpoint.sendText(JSON.toJSONString(ImmutableMap.of("driverSessionId", androidDriver.getSessionId().toString())));
    }

    @OnClose
    public void onClose() {
        log.info("[android-stf-websocket][{}]onClose", mobileId);

        if (androidDevice != null) {
            WebSocketSessionPool.remove(mobileId);
            androidDevice.getMinitouch().stop();
            androidDevice.getMinicap().stop();
            androidDevice.quitAppiumDriver();
            mobileService.saveIdleDeviceToServer(androidDevice);
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[android-stf-websocket][{}]onError", mobileId, t);
    }


    @OnMessage
    public void onMessage(String message) {
        handleMessage(message);
    }

    /**
     * 根据前端传来的操作，执行相应操作
     *
     * @param msg
     */
    private void handleMessage(String msg) {
        JSONObject message = JSON.parseObject(msg);
        String operation = message.getString("operation");
        switch (operation) {
            case "m":
                androidDevice.getMinitouch()
                        .moveTo(message.getInteger("x"), message.getInteger("y"), message.getInteger("width"), message.getInteger("height"));
                break;
            case "d":
                androidDevice.getMinitouch()
                        .touchDown(message.getInteger("x"), message.getInteger("y"), message.getInteger("width"), message.getInteger("height"));
                break;
            case "u":
                androidDevice.getMinitouch().touchUp();
                break;
            case "home":
                androidDriver.pressKey(new KeyEvent(AndroidKey.HOME));
                break;
            case "back":
                androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
                break;
            case "power":
                androidDriver.pressKey(new KeyEvent(AndroidKey.POWER));
                break;
            case "menu":
                androidDriver.pressKey(new KeyEvent(AndroidKey.MENU));
                break;
        }
    }
}