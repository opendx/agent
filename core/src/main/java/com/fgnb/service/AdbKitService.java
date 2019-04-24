package com.fgnb.service;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.stf.adbkit.AdbKit;
import com.fgnb.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Service
@Slf4j
public class AdbKitService {

    public Response start(String deviceId) {
        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if(androidDevice == null) {
            return Response.fail("设备不存在");
        }
        if(!androidDevice.isConnected()) {
            return Response.fail("设备未连接");
        }
        AdbKit adbKit = new AdbKit(androidDevice);
        try {
            adbKit.start();
        } catch (IOException e) {
            log.error("启动adb kit失败");
        }
        return Response.success("123");
    }
}
