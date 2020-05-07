package com.daxiang.service;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.model.Mobile;
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

    public Response screenshot(String mobileId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(mobileId);
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

    public Response dump(String mobileId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(mobileId);
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

    public Response installApp(MultipartFile app, String mobileId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(mobileId);
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
        Mobile mobile = mobileDevice.getMobile();
        mobile.setAgentIp(ip);
        mobile.setAgentPort(port);
        mobile.setStatus(Mobile.IDLE_STATUS);
        mobile.setLastOnlineTime(new Date());
        log.info("saveOnlineDeviceToServer: {}", mobile);
        serverClient.saveDevice(mobile);
    }

    public void saveUsingDeviceToServer(MobileDevice mobileDevice) {
        if (mobileDevice.isConnected()) {
            Mobile mobile = mobileDevice.getMobile();
            mobile.setStatus(Mobile.USING_STATUS);
            log.info("saveUsingDeviceToServer: {}", mobile);
            serverClient.saveDevice(mobile);
        }
    }

    public void saveIdleDeviceToServer(MobileDevice mobileDevice) {
        if (mobileDevice.isConnected()) {
            Mobile mobile = mobileDevice.getMobile();
            mobile.setStatus(Mobile.IDLE_STATUS);
            log.info("saveIdleDeviceToServer: {}", mobile);
            serverClient.saveDevice(mobile);
        }
    }

    public void saveOfflineDeviceToServer(MobileDevice mobileDevice) {
        Mobile mobile = mobileDevice.getMobile();
        mobile.setStatus(Mobile.OFFLINE_STATUS);
        log.info("saveOfflineDeviceToServer: {}", mobile);
        serverClient.saveDevice(mobile);
    }

    public Response getStatus(String mobileId) {
        if (StringUtils.isEmpty(mobileId)) {
            List<Mobile> mobiles = MobileDeviceHolder.getAll().stream()
                    .map(MobileDevice::getMobile).collect(Collectors.toList());
            return Response.success(mobiles);
        } else {
            MobileDevice mobileDevice = MobileDeviceHolder.get(mobileId);
            return Objects.isNull(mobileDevice) ? Response.success() : Response.success(mobileDevice.getMobile());
        }
    }
}
