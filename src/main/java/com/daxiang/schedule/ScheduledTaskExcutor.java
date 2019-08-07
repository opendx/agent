package com.daxiang.schedule;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.api.MasterApi;
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
        List<String> idleMobileDeviceIds = MobileDeviceHolder.getAll().stream()
                .filter(MobileDevice::isIdle)
                .map(MobileDevice::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idleMobileDeviceIds)) {
            return;
        }

        idleMobileDeviceIds.stream().parallel().forEach(deviceId -> {
            DeviceTestTask unStartDeviceTestTask = masterApi.getFirstUnStartDeviceTestTask(deviceId);
            if (unStartDeviceTestTask != null) {
                MobileDeviceHolder.get(deviceId).getDeviceTestTaskExcutor().commitTestTask(unStartDeviceTestTask);
                log.info("[{}][自动化测试]提交测试任务: {}", deviceId, unStartDeviceTestTask.getTestTaskName());
            }
        });
    }
}
