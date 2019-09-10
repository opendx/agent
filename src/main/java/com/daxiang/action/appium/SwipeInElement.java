package com.daxiang.action.appium;

import com.alibaba.fastjson.JSON;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * Created by jiangyitao.
 * 元素内滑动
 */
@Slf4j
public class SwipeInElement {

    private AppiumDriver appiumDriver;

    public SwipeInElement(AppiumDriver appiumDriver) {
        this.appiumDriver = appiumDriver;
    }

    public void execute(Object webElement, Object startX, Object startY, Object endX, Object endY) {
        Assert.notNull(webElement, "元素不能为空");
        Assert.notNull(startX, "X起点不能为空");
        Assert.notNull(startY, "Y起点不能为空");
        Assert.notNull(endX, "X终点不能为空");
        Assert.notNull(endY, "Y终点不能为空");

        WebElement element = (WebElement) webElement;

        Rectangle rect = element.getRect();
        int elementHeight = rect.getHeight();
        int elementWidth = rect.getWidth();
        log.info("element rect: {}", JSON.toJSONString(rect));

        // 左上角坐标
        Point point = rect.getPoint();
        int topLeftX = point.getX();
        int topLeftY = point.getY();

        int _startX = topLeftX + (int) (Float.parseFloat((String) startX) * elementWidth);
        int _startY = topLeftY + (int) (Float.parseFloat((String) startY) * elementHeight);
        int _endX = topLeftX + (int) (Float.parseFloat((String) endX) * elementWidth);
        int _endY = topLeftY + (int) (Float.parseFloat((String) endY) * elementHeight);

        log.info("在element内滑动: ({},{}) -> ({},{})", _startX, _startY, _endX, _endY);
        new TouchAction(appiumDriver)
                .press(PointOption.point(_startX, _startY))
                .waitAction(WaitOptions.waitOptions(Duration.ZERO))
                .moveTo(PointOption.point(_endX, _endY))
                .release()
                .perform();
    }
}
