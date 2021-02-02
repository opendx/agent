package com.daxiang.service;

import com.daxiang.core.Device;
import com.daxiang.core.mobile.Mobile;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.core.DeviceHolder;
import com.daxiang.exception.AgentException;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class MobileService {

    public void installApp(MultipartFile app, String mobileId) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            throw new AgentException(mobileId + "未连接");
        }

        String fileName = app.getOriginalFilename();
        fileName = UUIDUtil.getUUIDFilename(fileName);
        File appFile = new File(fileName);

        try {
            FileUtils.copyInputStreamToFile(app.getInputStream(), appFile);
            ((MobileDevice) device).installApp(appFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("[{}]安装app失败", mobileId, e);
            throw new AgentException(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(appFile);
        }
    }

    public Mobile getMobile(String mobileId) {
        if (StringUtils.isEmpty(mobileId)) {
            throw new AgentException("mobileId不能为空");
        }

        Device device = DeviceHolder.get(mobileId);
        return device == null ? null : ((MobileDevice) device).getMobile();
    }

    public Mobile delete(String mobileId) {
        if (StringUtils.isEmpty(mobileId)) {
            throw new AgentException("mobileId不能为空");
        }

        Device device = DeviceHolder.remove(mobileId);
        if (device == null) {
            return null;
        }

        device.getDeviceServer().stop();
        return ((MobileDevice) device).getMobile();
    }

    public String startLogsBroadcast(String mobileId, String sessionId) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            throw new AgentException(mobileId + "未连接");
        }

        return ((MobileDevice) device).startLogsBroadcast(sessionId);
    }

    public void stopLogsBroadcast(String mobileId) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            throw new AgentException(mobileId + "未连接");
        }

        ((MobileDevice) device).stopLogsBroadcast();
    }
}
