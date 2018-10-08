package com.fgnb.android.uiautomator;

import com.fgnb.android.AndroidPort;
import com.fgnb.android.PortProvider;

/**
 * Created by jiangyitao.
 * minitouch 端口提供器
 */
public class UiAutomatorServerPortProvider {

    private static PortProvider portProvider;

    static {
        portProvider = new PortProvider(AndroidPort.MACACASERVER_PORT_START, AndroidPort.MACACASERVER_PORT_END);
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
