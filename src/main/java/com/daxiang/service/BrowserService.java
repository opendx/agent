package com.daxiang.service;

import com.daxiang.core.Device;
import com.daxiang.core.DeviceHolder;
import com.daxiang.core.pc.web.Browser;
import com.daxiang.core.pc.web.BrowserDevice;
import com.daxiang.exception.AgentException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by jiangyitao.
 */
@Service
public class BrowserService {

    public Browser getBrowser(String browserId) {
        if (StringUtils.isEmpty(browserId)) {
            throw new AgentException("browserId不能为空");
        }

        Device device = DeviceHolder.get(browserId);
        return device == null ? null : ((BrowserDevice) device).getBrowser();
    }

}
