package com.fgnb.schedule;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.api.MasterApi;
import com.fgnb.model.Device;
import com.fgnb.model.devicetesttask.DeviceTestTask;
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
        //在线闲置的设备
        List<String> idleAndroidDeviceIds = AndroidDeviceHolder.getAndroidDevices().stream()
                .filter(androidDevice -> androidDevice.getDevice().getStatus() == Device.IDLE_STATUS)
                .map(AndroidDevice::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idleAndroidDeviceIds)) {
            return;
        }
        //还没开始的测试任务
        List<DeviceTestTask> unStartDeviceTestTasks = masterApi.getUnStartTestTasksByDeviceIds(idleAndroidDeviceIds);

        unStartDeviceTestTasks.stream().parallel().forEach(deviceTestTask -> {
            AndroidDeviceHolder.get(deviceTestTask.getDeviceId()).commitTestTask(deviceTestTask);
            log.info("[{}][自动化测试]提交测试任务{}",deviceTestTask.getDeviceId(),deviceTestTask.getTestTaskName());
        });
    }
}
