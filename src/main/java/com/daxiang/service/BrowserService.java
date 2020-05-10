package com.daxiang.service;

import com.daxiang.core.Device;
import com.daxiang.core.DeviceHolder;
import com.daxiang.core.pc.web.BrowserDevice;
import com.daxiang.model.Response;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by jiangyitao.
 */
@Service
public class BrowserService {

    public Response getBrowser(String browserId) {
        if (StringUtils.isEmpty(browserId)) {
            return Response.fail("browserId不能为空");
        }

        Device device = DeviceHolder.get(browserId);
        if (device == null) {
            return Response.success();
        } else {
            BrowserDevice browserDevice = (BrowserDevice) device;
            return Response.success(browserDevice.getBrowser());
        }
    }

}
