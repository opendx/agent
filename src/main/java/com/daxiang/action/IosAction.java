package com.daxiang.action;

import com.daxiang.core.mobile.ios.IosDevice;
import com.daxiang.core.mobile.ios.IosUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 * id 3000 - 3999
 * platforms = [2]
 */
@Slf4j
public class IosAction extends MobileAction {

    public IosAction(IosDevice iosDevice) {
        super(iosDevice);
    }

    /**
     * 3000.启动/重启 app
     *
     * @param bundleId
     */
    public void restartIosApp(String bundleId) {
        Assert.hasText(bundleId, "bundleId不能为空");

        RemoteWebDriver driver = device.getDriver();
        IosUtil.terminateApp(driver, bundleId);
        IosUtil.launchApp(driver, bundleId);
    }
}
