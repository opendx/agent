package com.fgnb.service;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class TaskService {


    public void commit(String deviceId, Map<String,String> codes){
        log.info("[{}]提交测试任务",deviceId);
        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if(androidDevice == null){
            throw new RuntimeException("设备不存在");
        }
        androidDevice.addTask(codes);
    }

}
