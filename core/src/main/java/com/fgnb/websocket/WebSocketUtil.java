package com.fgnb.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.websocket.Session;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class WebSocketUtil {

    public static void sendText(Session session,String msg) throws Exception{
        if(session == null){
            throw new RuntimeException("session不能为空");
        }
        if(!session.isOpen()){
            throw new RuntimeException("session没有打开");
        }
        if(StringUtils.isEmpty(msg)){
            throw new RuntimeException("信息不能为空");
        }
        try {
            session.getBasicRemote().sendText(msg);
        }catch (IOException e){
            log.error("发送信息失败",e);
            throw e;
        }
    }
}
