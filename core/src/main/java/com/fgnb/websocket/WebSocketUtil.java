package com.fgnb.websocket;

import org.springframework.util.StringUtils;

import javax.websocket.Session;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class WebSocketUtil {

    public static void sendText(Session session, String msg) throws IOException {
        if (session == null) {
            throw new RuntimeException("session不能为空");
        }
        if (!session.isOpen()) {
            throw new RuntimeException("session没有打开");
        }
        if (StringUtils.isEmpty(msg)) {
            throw new RuntimeException("信息不能为空");
        }
        session.getBasicRemote().sendText(msg);
    }
}
