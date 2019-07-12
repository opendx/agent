package com.daxiang.action;

import io.appium.java_client.AppiumDriver;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
public class SetImplicitlyWaitTime {

    private AppiumDriver driver;

    public SetImplicitlyWaitTime(AppiumDriver driver) {
        this.driver = driver;
    }

    public void excute(Object implicitlyWaitTime) {
        Assert.notNull(implicitlyWaitTime, "隐士等待时间不能为空");
        long _implicitlyWaitTime = Long.parseLong((String) implicitlyWaitTime);
        driver.manage().timeouts().implicitlyWait(_implicitlyWaitTime, TimeUnit.SECONDS);
    }
}
