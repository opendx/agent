package com.daxiang.core.ios;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class IosDeviceMonitor {

    private static final IosDeviceMonitor INSTANCE = new IosDeviceMonitor();

    private List<String> lastDeviceList = new ArrayList<>();
    private List<String> currentDeviceList = new ArrayList<>();
    /**
     * 每5秒检查一次
     */
    private static final int MONITOR_PERIOD = 5 * 1000;
    private boolean isMonitorDevice = false;

    private IosDeviceMonitor() {
    }

    public static IosDeviceMonitor getInstance() {
        return INSTANCE;
    }

    public void start(IosDeviceChangeListener iosDeviceChangeListener) {
        if (isMonitorDevice) {
            return;
        }
        isMonitorDevice = true;
        new Thread(() -> {
            while (isMonitorDevice) {
                if(iosDeviceChangeListener != null) {
                    currentDeviceList = IosUtil.getDeviceList();

                    // 新增的设备
                    currentDeviceList.stream()
                            .filter(deviceId -> !lastDeviceList.contains(deviceId))
                            .forEach(deviceId -> iosDeviceChangeListener.onDeviceConnected(deviceId));
                    // 减少的设备
                    lastDeviceList.stream()
                            .filter(deviceId -> !currentDeviceList.contains(deviceId))
                            .forEach(deviceId -> iosDeviceChangeListener.onDeviceDisConnected(deviceId));

                    lastDeviceList = currentDeviceList;
                }

                try {
                    Thread.sleep(MONITOR_PERIOD);
                } catch (InterruptedException e) {
                    log.error("sleep err", e);
                }
            }
        }).start();
    }

    public void stop() {
        isMonitorDevice = false;
    }
}
