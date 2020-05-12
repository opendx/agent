package com.daxiang.schedule;

import com.daxiang.core.Device;
import com.daxiang.core.DeviceHolder;
import com.daxiang.server.ServerClient;
import com.daxiang.model.devicetesttask.DeviceTestTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class ScheduledTaskExecutor {

    @Autowired
    private ServerClient serverClient;

    /**
     * 定时检测device的测试任务
     */
    @Scheduled(fixedRate = 10000)
    public void commitDeviceTestTask() {
        // 在线闲置的device
        List<Device> devices = DeviceHolder.getAll().stream()
                .filter(Device::isIdle)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(devices)) {
            return;
        }

        devices.stream().parallel().forEach(device -> {
            // 获取最早的一个未开始的任务
            DeviceTestTask deviceTestTask = serverClient.getFirstUnStartDeviceTestTask(device.getId());
            if (deviceTestTask != null) {
                log.info("[{}]提交测试任务, deviceTestTaskId: {}", device.getId(), deviceTestTask.getId());
                device.getDeviceTestTaskExecutor().commitTestTask(deviceTestTask);
            }
        });
    }
}
