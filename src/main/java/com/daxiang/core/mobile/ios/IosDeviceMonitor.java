package com.daxiang.core.mobile.ios;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class IosDeviceMonitor {

    private static final IosDeviceMonitor INSTANCE = new IosDeviceMonitor();

    private Set<String> lastRealDeviceSet = new HashSet<>();
    private Set<String> currentRealDeviceSet = new HashSet<>();

    private Set<String> lastSimulatorSet = new HashSet<>();
    private Set<String> currentSimulatorSet = new HashSet<>();

    private final ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
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

        scheduledService.scheduleAtFixedRate(() -> {

            // 真机
            currentRealDeviceSet = IosUtil.getRealDeviceList(false);
            // 新增的真机
            currentRealDeviceSet.stream()
                    .filter(deviceId -> !lastRealDeviceSet.contains(deviceId))
                    .forEach(deviceId -> iosDeviceChangeListener.deviceConnected(new IosIDevice(deviceId, true)));
            // 减少的真机
            lastRealDeviceSet.stream()
                    .filter(deviceId -> !currentRealDeviceSet.contains(deviceId))
                    .forEach(deviceId -> iosDeviceChangeListener.deviceDisconnected(new IosIDevice(deviceId, true)));
            lastRealDeviceSet = currentRealDeviceSet;

            // 模拟器
            currentSimulatorSet = IosUtil.getSimulatorList(false);
            // 新增的模拟器
            currentSimulatorSet.stream()
                    .filter(deviceId -> !lastSimulatorSet.contains(deviceId))
                    .forEach(deviceId -> iosDeviceChangeListener.deviceConnected(new IosIDevice(deviceId, false)));
            // 减少的模拟器
            lastSimulatorSet.stream()
                    .filter(deviceId -> !currentSimulatorSet.contains(deviceId))
                    .forEach(deviceId -> iosDeviceChangeListener.deviceDisconnected(new IosIDevice(deviceId, false)));
            lastSimulatorSet = currentSimulatorSet;

        }, 0, MONITOR_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduledService.shutdown();
    }
}
