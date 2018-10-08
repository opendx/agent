package com.fgnb.android.stf.adbkit;

import com.fgnb.android.AndroidPort;
import com.fgnb.android.PortProvider;

/**
 * Created by jiangyitao.
 * adbkit 端口提供器
 */
public class AdbKitPortProvider {

    private static PortProvider portProvider;

    static {
        portProvider = new PortProvider(AndroidPort.ADBKIT_PORT_START, AndroidPort.ADBKIT_PORT_END);
    }

    /**
     * 获取可用的端口
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
