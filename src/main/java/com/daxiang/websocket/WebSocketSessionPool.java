package com.daxiang.websocket;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
public class WebSocketSessionPool {

    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    public static void put(String id, Session session) {
        SESSION_POOL.put(id, session);
    }

    public static void remove(String id) {
        SESSION_POOL.remove(id);
    }

    public static Session getOpenedSession(String id) {
        Session session = SESSION_POOL.get(id);
        if (session != null && session.isOpen()) {
            return session;
        } else {
            return null;
        }
    }
}
