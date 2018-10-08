package com.fgnb.android.stf.minitouch;

import com.fgnb.android.AndroidPort;
import com.fgnb.android.PortProvider;

/**
 * Created by jiangyitao.
 * minitouch 端口提供器
 */
public class MinitouchPortProvider {

    private static PortProvider portProvider;

    static {
        portProvider = new PortProvider(AndroidPort.MINITOUCH_PORT_START, AndroidPort.MINITOUCH_PORT_END);
    }

    /**
     * @return
     * @throws Exception
     */
    public static synchronized int getAvailablePort() throws Exception{
        return portProvider.getAvailablePorts();
    }


    /**
     * 归还端口
     * @param port
     * @return
     */
    public static boolean pushAvailablePort(int port){
        return portProvider.pushAvailablePorts(port);
    }

}
