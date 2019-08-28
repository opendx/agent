package com.daxiang.action.appium;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class SwipeScreen {

    private AppiumDriver appiumDriver;

    public SwipeScreen(AppiumDriver appiumDriver) {
        this.appiumDriver = appiumDriver;
    }

    public void excute(Object startX, Object startY, Object endX, Object endY) {
        Assert.notNull(startX, "X起点不能为空");
        Assert.notNull(startY, "Y起点不能为空");
        Assert.notNull(endX, "X终点不能为空");
        Assert.notNull(endY, "Y终点不能为空");

        MobileDevice mobileDevice = MobileDeviceHolder.getMobileDeviceByAppiumDriver(appiumDriver);
        if (mobileDevice == null) {
            throw new RuntimeException("手机不存在");
        }

        int screenHeight = mobileDevice.getDevice().getScreenHeight();
        int screenWidth = mobileDevice.getDevice().getScreenWidth();

        int _startX = (int) (Float.parseFloat((String) startX) * screenWidth);
        int _startY = (int) (Float.parseFloat((String) startY) * screenHeight);
        int _endX = (int) (Float.parseFloat((String) endX) * screenWidth);
        int _endY = (int) (Float.parseFloat((String) endY) * screenHeight);

        log.info("滑动屏幕: ({},{}) -> ({},{})", _startX, _startY, _endX, _endY);
        new TouchAction(appiumDriver)
                .press(PointOption.point(_startX, _startY))
                .waitAction(WaitOptions.waitOptions(Duration.ZERO))
                .moveTo(PointOption.point(_endX, _endY))
                .release()
                .perform();
    }
}
