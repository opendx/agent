package com.daxiang.websocket;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
public class MobileDeviceWebSocketSessionPool {

    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    public static void put(String deviceId, Session session) {
        SESSION_POOL.put(deviceId, session);
    }

    public static void remove(String deviceId) {
        SESSION_POOL.remove(deviceId);
    }

    public static Session getOpenedSession(String deviceId) {
        Session session = SESSION_POOL.get(deviceId);
        if (session != null && session.isOpen()) {
            return session;
        } else {
            return null;
        }
    }
}
