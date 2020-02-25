package com.daxiang.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class NetUtil {

    /**
     * 检测本地端口是否可用
     *
     * @param port 端口号
     * @return
     */
    public static boolean isPortAvailable(int port) {
        try {
            bindPort("127.0.0.1", port);
            bindPort("0.0.0.0", port);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void bindPort(String ip, int port) throws IOException {
        try (Socket socket = new Socket()) {
            socket.bind(new InetSocketAddress(ip, port));
        }
    }

    public static int getAvailablePort(int startPort, int endPort, int port) {
        if (startPort >= endPort) {
            throw new IllegalArgumentException();
        }

        while (true) {
            if (port > endPort || port < startPort) {
                port = startPort;
            }
            if (isPortAvailable(port)) {
                return port;
            } else {
                port++;
            }
        }
    }
}
