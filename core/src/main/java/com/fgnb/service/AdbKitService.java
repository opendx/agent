package com.fgnb.service;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Service
@Slf4j
public class AdbKitService {

    public Response start(String deviceId) {
        if(StringUtils.isEmpty(deviceId)) {
            return Response.fail("设备id不能为空");
        }
        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if(androidDevice == null) {
            return Response.fail("设备不存在");
        }
        if(!androidDevice.isConnected()) {
            return Response.fail("设备未连接");
        }
        try {
            int port = androidDevice.getAdbKit().start();
            return Response.success(port);
        } catch (IOException e) {
            log.error("启动adbkit失败",e);
            return Response.fail(e.getMessage());
        }
    }

    public Response stop(String deviceId) {
        if(StringUtils.isEmpty(deviceId)) {
            return Response.fail("设备id不能为空");
        }
        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if(androidDevice == null) {
            return Response.fail("设备不存在");
        }
        if(!androidDevice.isConnected()) {
            return Response.fail("设备未连接");
        }
        androidDevice.getAdbKit().stop();
        return Response.success("停止完成");
    }
}
