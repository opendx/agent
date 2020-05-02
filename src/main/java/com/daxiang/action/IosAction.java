package com.daxiang.action;

import com.daxiang.core.ios.IosUtil;
import io.appium.java_client.ios.IOSDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 * id 3000 - 3999
 * platforms = [2]
 */
@Slf4j
public class IosAction extends MobileAction {

    private IOSDriver driver;

    public IosAction(IOSDriver driver) {
        super(driver);
        this.driver = driver;
    }

    /**
     * 3000.启动/重启 app
     *
     * @param bundleId
     */
    public void restartIosApp(String bundleId) {
        Assert.hasText(bundleId, "bundleId不能为空");

        IosUtil.terminateApp(driver, bundleId);
        IosUtil.launchApp(driver, bundleId);
    }
}
