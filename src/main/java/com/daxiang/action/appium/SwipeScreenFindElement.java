package com.daxiang.action.appium;

import com.daxiang.action.utils.ByUtil;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * Created by jiangyitao.
 * 滑动屏幕查找元素
 */
@Slf4j
public class SwipeScreenFindElement {

    private AppiumDriver appiumDriver;

    public SwipeScreenFindElement(AppiumDriver appiumDriver) {
        this.appiumDriver = appiumDriver;
    }

    public WebElement execute(Object findBy, Object value, Object startX, Object startY, Object endX, Object endY, Object maxSwipeCount) {
        Assert.notNull(findBy, "findBy不能为空");
        Assert.notNull(value, "value不能为空");
        Assert.notNull(startX, "X起点不能为空");
        Assert.notNull(startY, "Y起点不能为空");
        Assert.notNull(endX, "X终点不能为空");
        Assert.notNull(endY, "Y终点不能为空");
        Assert.notNull(maxSwipeCount, "最大滑动次数不能为空");

        String _findBy = (String) findBy;
        String _value = (String) value;
        By by = ByUtil.getBy(_findBy, _value);

        try {
            return appiumDriver.findElement(by);
        } catch (Exception e) {
        }

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

        int _maxSwipeCount = Integer.parseInt((String) maxSwipeCount);

        for (int i = 0; i < _maxSwipeCount; i++) {
            log.info("滑动第{}次屏幕: ({},{}) -> ({},{})", i + 1, _startX, _startY, _endX, _endY);
            new TouchAction(appiumDriver)
                    .press(PointOption.point(_startX, _startY))
                    .waitAction(WaitOptions.waitOptions(Duration.ZERO))
                    .moveTo(PointOption.point(_endX, _endY))
                    .release()
                    .perform();
            try {
                return appiumDriver.findElement(by);
            } catch (Exception e) {
            }
        }

        return appiumDriver.findElement(by);
    }
}
