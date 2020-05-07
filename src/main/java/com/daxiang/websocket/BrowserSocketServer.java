package com.daxiang.websocket;

import com.alibaba.fastjson.JSON;
import com.daxiang.App;
import com.daxiang.core.pc.web.Browser;
import com.daxiang.core.pc.web.BrowserHolder;
import com.daxiang.service.BrowserService;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
@ServerEndpoint(value = "/browser/{browserId}/user/{username}")
public class BrowserSocketServer {

    private Browser browser;
    private String browserId;
    private BrowserService browserService;

    @OnOpen
    public void onOpen(@PathParam("browserId") String browserId, @PathParam("username") String username, Session session) throws Exception {
        log.info("[browser-websocket][{}]onOpen: username -> {}", browserId, username);
        this.browserId = browserId;

        RemoteEndpoint.Basic remoteEndpoint = session.getBasicRemote();
        remoteEndpoint.sendText("browser websocket连接成功");

        browser = BrowserHolder.getIdleBrowser(browserId);
        if (browser == null) {
            remoteEndpoint.sendText("浏览器未处于闲置状态，无法使用");
            session.close();
            return;
        }

        Session openedSession = WebSocketSessionPool.getOpenedSession(browserId);
        if (openedSession != null) {
            remoteEndpoint.sendText("浏览器" + browserId + "正在被" + openedSession.getId() + "连接占用，请稍后重试");
            session.close();
            return;
        }

        WebSocketSessionPool.put(browserId, session);

        browserService = App.getBean(BrowserService.class);

        browser.setUsername(username);
        browserService.saveUsingBrowserToServer(browser);

        remoteEndpoint.sendText("初始化driver...");
        RemoteWebDriver driver = browser.freshDriver();
        remoteEndpoint.sendText("初始化driver完成");

        remoteEndpoint.sendText(JSON.toJSONString(ImmutableMap.of("driverSessionId", driver.getSessionId().toString())));
    }

    @OnClose
    public void onClose() {
        log.info("[browser-websocket][{}]onClose", browserId);

        if (browserService != null) {
            WebSocketSessionPool.remove(browserId);
            browser.quitDriver();
            browserService.saveIdleBrowserToServer(browser);
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[browser-websocket][{}]onError", browserId, t);
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("[browser-websocket][{}]message: {}", browserId, message);
    }
}