package com.fgnb.service;

import com.alibaba.fastjson.JSONObject;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class Uiautomator2ServerService {


    public Response start(String deviceId) {
        if(StringUtils.isEmpty(deviceId)) {
            return Response.fail("deviceId不能为空");
        }
        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if(androidDevice == null) {
            return Response.fail("设备不存在");
        }
        if(!androidDevice.isConnected()) {
            return Response.fail("设备未连接");
        }
        try {
            int port = androidDevice.getUiautomator2Server().start();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("port",port);
            return Response.success(jsonObject);
        } catch (Exception e) {
            log.error("[{}]启动uiautomator2server出错",deviceId,e);
            return Response.fail(e.getMessage());
        }
    }
}
