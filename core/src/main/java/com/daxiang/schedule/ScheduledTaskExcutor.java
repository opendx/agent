package com.daxiang.schedule;

import com.daxiang.android.AndroidDevice;
import com.daxiang.android.AndroidDeviceHolder;
import com.daxiang.api.MasterApi;
import com.daxiang.model.Device;
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
public class ScheduledTaskExcutor {

    @Autowired
    private MasterApi masterApi;

    /**
     * 定时检测设备的测试任务
     */
    @Scheduled(fixedRate = 10000)
    public void commitDeviceTestTask() {
        // 在线闲置的设备
        List<String> idleAndroidDeviceIds = AndroidDeviceHolder.getAndroidDevices().stream()
                .filter(androidDevice -> androidDevice.getDevice().getStatus() == Device.IDLE_STATUS)
                .map(AndroidDevice::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idleAndroidDeviceIds)) {
            return;
        }

        idleAndroidDeviceIds.stream().parallel().forEach(deviceId -> {
            DeviceTestTask unStartDeviceTestTask = masterApi.getFirstUnStartDeviceTestTask(deviceId);
            if (unStartDeviceTestTask != null) {
                AndroidDeviceHolder.get(deviceId).commitTestTask(unStartDeviceTestTask);
                log.info("[{}][自动化测试]提交测试任务: {}", deviceId, unStartDeviceTestTask.getTestTaskName());
            }
        });
    }
}
