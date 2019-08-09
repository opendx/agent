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
        List<MobileDevice> idleMobileDevices = MobileDeviceHolder.getAll().stream()
                .filter(MobileDevice::isIdle)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idleMobileDevices)) {
            return;
        }

        idleMobileDevices.stream().parallel().forEach(idleMobileDevice -> {
            // 获取最早的一个未开始的设备测试任务
            DeviceTestTask unStartDeviceTestTask = masterApi.getFirstUnStartDeviceTestTask(idleMobileDevice.getId());
            if (unStartDeviceTestTask != null) {
                idleMobileDevice.getDeviceTestTaskExcutor().commitTestTask(unStartDeviceTestTask);
                log.info("[自动化测试][{}]提交测试任务: {}", idleMobileDevice.getId(), unStartDeviceTestTask.getTestTaskName());
            }
        });
    }
}
