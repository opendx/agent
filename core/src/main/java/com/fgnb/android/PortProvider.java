package com.fgnb.android;

import com.fgnb.utils.NetUtil;


/**
 * Created by jiangyitao.
 */
public class PortProvider {

    private static final int MINITOUCH_PORT_START = 7000;
    private static final int MINITOUCH_PORT_END = 7499;
    private static int minitouchPort = MINITOUCH_PORT_START;

    private static final int MINICAP_PORT_START = 7500;
    private static final int MINICAP_PORT_END = 7999;
    private static int minicapPort = MINICAP_PORT_START;

    private static final int ADBKIT_PORT_START = 8000;
    private static final int ADBKIT_PORT_END = 8499;
    private static int adbKitPort = ADBKIT_PORT_START;

    private static final int UIAUTOMATOR2_SERVER_PORT_START = 8500;
    private static final int UIAUTOMATOR2_SERVER_PORT_END = 8999;
    private static int uiautomator2ServerPort = UIAUTOMATOR2_SERVER_PORT_START;

    public static synchronized int getMinitouchAvailablePort() {
        int availablePort = getAvailablePort(MINITOUCH_PORT_START, MINITOUCH_PORT_END, minitouchPort);
        minitouchPort = availablePort + 1;
        return availablePort;
    }

    public static synchronized int getMinicapAvailablePort() {
        int availablePort = getAvailablePort(MINICAP_PORT_START, MINICAP_PORT_END, minicapPort);
        minicapPort = availablePort + 1;
        return availablePort;
    }

    public static synchronized int getAdbKitAvailablePort() {
        int availablePort = getAvailablePort(ADBKIT_PORT_START, ADBKIT_PORT_END, adbKitPort);
        adbKitPort = availablePort + 1;
        return availablePort;
    }

    public static synchronized int getUiautomator2ServerPort() {
        int availablePort = getAvailablePort(UIAUTOMATOR2_SERVER_PORT_START, UIAUTOMATOR2_SERVER_PORT_END, uiautomator2ServerPort);
        uiautomator2ServerPort = availablePort + 1;
        return availablePort;
    }

    private static int getAvailablePort(int startPort,int endPort,int port) {
        while (true) {
            if(port > endPort || port < startPort) {
                port = startPort;
            }
            if(NetUtil.isPortAvailable(port)) {
                return port;
            } else {
                port ++;
            }
        }
    }


}
