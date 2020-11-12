package com.daxiang.core.mobile;

import com.daxiang.core.Device;
import com.daxiang.core.mobile.appium.AppiumNativePageSourceHandler;
import com.daxiang.model.page.Page;
import com.daxiang.core.mobile.appium.AppiumServer;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.json.XML;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Created by jiangyitao.
 */
@Slf4j
public abstract class MobileDevice extends Device {

    /**
     * 调试action需要，两次命令之间最大允许12小时间隔
     */
    public static final int NEW_COMMAND_TIMEOUT = 60 * 60 * 12;

    public static final String NATIVE_CONTEXT = "NATIVE_APP";

    public static final int PLATFORM_ANDROID = 1;
    public static final int PLATFORM_IOS = 2;

    protected Mobile mobile;
    protected AppiumNativePageSourceHandler nativePageSourceHandler;

    public MobileDevice(Mobile mobile, AppiumServer appiumServer) {
        super(appiumServer);
        this.mobile = mobile;
    }

    public abstract void uninstallApp(String app) throws Exception;

    /**
     * @param app 本地路径 or 下载地址
     */
    public void installApp(String app) {
        ((AppiumDriver) driver).installApp(app);
    }

    @Override
    public Map<String, Object> dump() {
        if (isNativeContext()) { // 原生
            int type = isAndroid() ? Page.TYPE_ANDROID_NATIVE : Page.TYPE_IOS_NATIVE;
            String pageSource;
            try {
                pageSource = nativePageSourceHandler.handle(driver.getPageSource());
            } catch (IOException | DocumentException e) {
                throw new RuntimeException(e);
            }

            pageSource = XML.toJSONObject(pageSource).toString();
            return ImmutableMap.of("type", type, "pageSource", pageSource);
        } else { // webview
            return super.dump();
        }
    }

    @Override
    public void onlineToServer() {
        mobile.setAgentIp(agentIp);
        mobile.setAgentPort(agentPort);
        mobile.setLastOnlineTime(new Date());
        idleToServer();
    }

    @Override
    public void usingToServer(String username) {
        mobile.setUsername(username);
        mobile.setStatus(Device.USING_STATUS);
        serverClient.saveMobile(mobile);
    }

    @Override
    public void idleToServer() {
        mobile.setStatus(Device.IDLE_STATUS);
        serverClient.saveMobile(mobile);
    }

    @Override
    public void offlineToServer() {
        mobile.setStatus(Device.OFFLINE_STATUS);
        serverClient.saveMobile(mobile);
    }

    public Mobile getMobile() {
        return mobile;
    }

    @Override
    public String getId() {
        return mobile.getId();
    }

    @Override
    public Integer getStatus() {
        return mobile.getStatus();
    }

    public boolean isNativeContext() {
        return NATIVE_CONTEXT.equals(((AppiumDriver) driver).getContext());
    }

    public boolean isAndroid() {
        return mobile.getPlatform() == PLATFORM_ANDROID;
    }
}
