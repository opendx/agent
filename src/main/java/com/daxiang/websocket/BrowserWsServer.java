package com.daxiang.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
@ServerEndpoint(value = "/browser/{browserId}/user/{username}/project/{projectId}")
public class BrowserWsServer extends DeviceWsServer {

    @OnOpen
    public void onOpen(@PathParam("browserId") String browserId, @PathParam("username") String username,
                       @PathParam("projectId") Integer projectId, Session session) throws Exception {
        onWebsocketOpenStart(browserId, username, session);
        freshDriver(projectId);
        onWebsocketOpenFinish();
    }

    @OnClose
    public void onClose() {
        if (device != null) {
            onWebSocketClose();
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error("[{}]onError", deviceId, t);
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("[{}]message: {}", deviceId, message);
    }
}
