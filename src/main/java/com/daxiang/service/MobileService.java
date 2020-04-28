package com.daxiang.service;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.model.Device;
import com.daxiang.model.Response;
import com.daxiang.model.UploadFile;
import com.daxiang.server.ServerClient;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class MobileService {

    @Value("${ip}")
    private String ip;
    @Value("${port}")
    private Integer port;
    @Autowired
    private ServerClient serverClient;

    public Response screenshot(String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }

        UploadFile uploadFile = mobileDevice.screenshotAndUploadToServer();

        JSONObject response = new JSONObject();
        response.put("imgUrl", uploadFile.getDownloadUrl());
        response.put("imgPath", uploadFile.getFilePath());

        if (mobileDevice.isNativeContext()) {
            // 由于ios截图分辨率与dump的windowHierarchy bounds不一致，需要把当前屏幕信息传给前端处理
            // 在竖/横屏时，若android截图有虚拟按键，这里window的高度/宽度不包含虚拟按键
            Dimension window = mobileDevice.getAppiumDriver().manage().window().getSize();
            response.put("windowHeight", window.getHeight());
            response.put("windowWidth", window.getWidth());
            response.put("windowOrientation", mobileDevice.getAppiumDriver().getOrientation().value());
        }

        return Response.success(response);
    }

    public Response dump(String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }

        try {
            return Response.success(mobileDevice.dump());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    public Response installApp(MultipartFile app, String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }

        String fileName = app.getOriginalFilename();
        if (fileName.contains(".")) {
            fileName = UUIDUtil.getUUID() + "." + StringUtils.unqualify(fileName);
        } else {
            fileName = UUIDUtil.getUUID();
        }

        File appFile = new File(fileName);
        try {
            FileUtils.copyInputStreamToFile(app.getInputStream(), appFile);
            mobileDevice.installApp(appFile);
            return Response.success("安装成功");
        } catch (Exception e) {
            log.error("安装app失败", e);
            return Response.fail(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(appFile);
        }
    }

    public void saveOnlineDeviceToServer(MobileDevice mobileDevice) {
        Device device = mobileDevice.getDevice();
        device.setAgentIp(ip);
        device.setAgentPort(port);
        device.setStatus(Device.IDLE_STATUS);
        device.setLastOnlineTime(new Date());
        log.info("saveOnlineDeviceToServer: {}", device);
        serverClient.saveDevice(device);
    }

    public void saveUsingDeviceToServer(MobileDevice mobileDevice) {
        if (mobileDevice.isConnected()) {
            Device device = mobileDevice.getDevice();
            device.setStatus(Device.USING_STATUS);
            device.setUsername(device.getUsername());
            log.info("saveUsingDeviceToServer: {}", device);
            serverClient.saveDevice(device);
        }
    }

    public void saveIdleDeviceToServer(MobileDevice mobileDevice) {
        if (mobileDevice.isConnected()) {
            Device device = mobileDevice.getDevice();
            device.setStatus(Device.IDLE_STATUS);
            log.info("saveIdleDeviceToServer: {}", device);
            serverClient.saveDevice(device);
        }
    }

    public void saveOfflineDeviceToServer(MobileDevice mobileDevice) {
        Device device = mobileDevice.getDevice();
        device.setStatus(Device.OFFLINE_STATUS);
        device.setLastOfflineTime(new Date());
        log.info("saveOfflineDeviceToServer: {}", device);
        serverClient.saveDevice(device);
    }

    public Response getStatus(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            List<Device> devices = MobileDeviceHolder.getAll().stream()
                    .map(MobileDevice::getDevice).collect(Collectors.toList());
            return Response.success(devices);
        } else {
            MobileDevice mobileDevice = MobileDeviceHolder.get(deviceId);
            return Objects.isNull(mobileDevice) ? Response.success() : Response.success(mobileDevice.getDevice());
        }
    }
}
