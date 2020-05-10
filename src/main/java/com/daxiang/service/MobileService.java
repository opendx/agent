package com.daxiang.service;

import com.daxiang.core.Device;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.core.DeviceHolder;
import com.daxiang.model.Response;
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

    public Response installApp(MultipartFile app, String mobileId) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            return Response.fail(mobileId + "未连接");
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
            ((MobileDevice) device).installApp(appFile);
            return Response.success("安装成功");
        } catch (Exception e) {
            log.error("安装app失败", e);
            return Response.fail(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(appFile);
        }
    }

    public Response getMobile(String mobileId) {
        if (StringUtils.isEmpty(mobileId)) {
            return Response.fail("mobileId不能为空");
        }

        Device device = DeviceHolder.get(mobileId);
        if (device == null) {
            return Response.success();
        } else {
            MobileDevice mobileDevice = (MobileDevice) device;
            return Response.success(mobileDevice.getMobile());
        }
    }
}
