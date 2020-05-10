package com.daxiang.websocket;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class WebSocketSessionPool {

    // deviceId: Session
    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    public static void put(String devcieId, Session session) {
        SESSION_POOL.put(devcieId, session);
    }

    public static void remove(String devcieId) {
        SESSION_POOL.remove(devcieId);
    }

    public static Session getOpeningSession(String devcieId) {
        Session session = SESSION_POOL.get(devcieId);
        if (session != null && session.isOpen()) {
            return session;
        } else {
            return null;
        }
    }

    public static void closeOpeningSession(String deviceId) {
        Session openingSession = getOpeningSession(deviceId);
        if (openingSession != null) {
            try {
                openingSession.close();
            } catch (IOException e) {
                log.error("close session err, sessionId: {}", openingSession.getId());
            }
        }
    }
}
