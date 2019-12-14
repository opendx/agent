package com.daxiang.core.ios;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class IosDeviceMonitor {

    private static final IosDeviceMonitor INSTANCE = new IosDeviceMonitor();

    private List<String> lastDeviceList = new ArrayList<>();
    private List<String> currentDeviceList = new ArrayList<>();

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    /**
     * 每5秒检查一次
     */
    private static final int MONITOR_PERIOD_SECONDS = 5;

    private IosDeviceMonitor() {
    }

    public static IosDeviceMonitor getInstance() {
        return INSTANCE;
    }

    public void start(IosDeviceChangeListener iosDeviceChangeListener) {
        Assert.notNull(iosDeviceChangeListener, "iosDeviceChangeListener cannot be null");

        service.scheduleAtFixedRate(() -> {
            currentDeviceList = IosUtil.getDeviceList(false);

            // 新增的设备
            currentDeviceList.stream()
                    .filter(deviceId -> !lastDeviceList.contains(deviceId))
                    .forEach(deviceId -> iosDeviceChangeListener.onDeviceConnected(deviceId));
            // 减少的设备
            lastDeviceList.stream()
                    .filter(deviceId -> !currentDeviceList.contains(deviceId))
                    .forEach(deviceId -> iosDeviceChangeListener.onDeviceDisconnected(deviceId));

            lastDeviceList = currentDeviceList;
        }, 0, MONITOR_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        service.shutdown();
    }
}
