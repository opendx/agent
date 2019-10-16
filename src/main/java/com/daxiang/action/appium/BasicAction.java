package com.daxiang.action.appium;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidUtil;
import com.daxiang.core.ios.IosUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class BasicAction {

    public static final int EXECUTE_JAVA_CODE_ID = 1;

    private AppiumDriver driver;
    private int screenHeight;
    private int screenWidth;

    public BasicAction(AppiumDriver driver) {
        this.driver = driver;
        MobileDevice mobileDevice = MobileDeviceHolder.getMobileDeviceByAppiumDriver(driver);
        if (mobileDevice == null) {
            throw new RuntimeException("手机不存在");
        }
        screenHeight = mobileDevice.getDevice().getScreenHeight();
        screenWidth = mobileDevice.getDevice().getScreenWidth();
    }

    /**
     * 1.执行java代码
     * platform: Android / iOS
     *
     * @param javaCode
     */
    public void executeJavaCode(String javaCode) {
        Assert.hasText(javaCode, "javaCode不能为空");
    }

    /**
     * 2.卸载app
     * platform: Android / iOS
     *
     * @param packageName
     * @throws Exception
     */
    public void uninstallApp(String packageName) throws Exception {
        Assert.hasText(packageName, "apk包名或ios bundleId不能为空");

        if (driver instanceof AndroidDriver) {
            AndroidUtil.uninstallApk(MobileDeviceHolder.getIDeviceByAppiumDriver(driver), packageName);
        } else {
            IosUtil.uninstallApp(driver, packageName);
        }
    }

    /**
     * 3.安装app
     * platform: Android / iOS
     *
     * @param appDownloadUrl
     * @throws Exception
     */
    public void installApp(String appDownloadUrl) throws Exception {
        Assert.hasText(appDownloadUrl, "appDownloadUrl不能为空");

        MobileDeviceHolder.getMobileDeviceByAppiumDriver(driver).installApp(appDownloadUrl);
    }

    /**
     * platform: Android
     * 4.清除apk数据
     *
     * @param packageName
     * @throws Exception
     */
    public void clearApkData(String packageName) throws Exception {
        Assert.hasText(packageName, "包名不能为空");

        AndroidUtil.clearApkData(MobileDeviceHolder.getIDeviceByAppiumDriver(driver), packageName);
    }

    /**
     * platform: Android
     * 5.启动/重启 apk
     *
     * @param packageName
     * @param launchActivity
     * @throws Exception
     */
    public void restartApk(String packageName, String launchActivity) throws Exception {
        Assert.hasText(packageName, "包名不能为空");
        Assert.hasText(launchActivity, "启动Activity不能为空");

        AndroidUtil.restartApk(MobileDeviceHolder.getIDeviceByAppiumDriver(driver), packageName, launchActivity);
    }

    /**
     * platform: iOS
     * 6.启动/重启 app
     *
     * @param bundleId
     */
    public void restartIosApp(String bundleId) {
        Assert.hasText(bundleId, "bundleId不能为空");

        IosUtil.terminateApp(driver, bundleId);
        IosUtil.launchApp(driver, bundleId);
    }

    /**
     * platform: Android / iOS
     * 7.点击
     *
     * @param findBy
     * @param value
     * @return
     */
    public WebElement click(String findBy, String value) {
        WebElement element = findElement(findBy, value);
        element.click();
        return element;
    }

    /**
     * platform: Android / iOS
     * 8.查找元素
     *
     * @param findBy
     * @param value
     * @return
     */
    public WebElement findElement(String findBy, String value) {
        Assert.hasText(findBy, "findBy不能为空");
        Assert.hasText(value, "value不能为空");

        return driver.findElement(getBy(findBy, value));
    }

    /**
     * platform: Android / iOS
     * 9.查找元素
     *
     * @param findBy
     * @param value
     * @return 返回所有匹配的元素
     */
    public List<WebElement> findElements(String findBy, String value) {
        Assert.hasText(findBy, "findBy不能为空");
        Assert.hasText(value, "value不能为空");

        return driver.findElements(getBy(findBy, value));
    }

    /**
     * platform: Android / iOS
     * 10.输入
     *
     * @param findBy
     * @param value
     * @param content
     * @return
     */
    public WebElement sendKeys(String findBy, String value, String content) {
        Assert.hasText(findBy, "findBy不能为空");
        Assert.hasText(value, "value不能为空");
        Assert.hasText(content, "content不能为空");

        WebElement element = driver.findElement(getBy(findBy, value));
        element.sendKeys(content);
        return element;
    }

    /**
     * platform: Android / iOS
     * 11.设置隐式等待时间
     *
     * @param implicitlyWaitTimeInSeconds
     */
    public void setImplicitlyWaitTime(String implicitlyWaitTimeInSeconds) {
        Assert.hasText(implicitlyWaitTimeInSeconds, "隐式等待时间不能为空");

        driver.manage().timeouts().implicitlyWait(Long.parseLong(implicitlyWaitTimeInSeconds), TimeUnit.SECONDS);
    }

    /**
     * platform: Android / iOS
     * 12.等待元素可见
     *
     * @param findBy
     * @param value
     * @param maxWaitTimeInSeconds
     * @return
     */
    public WebElement waitForElementVisible(String findBy, String value, String maxWaitTimeInSeconds) {
        Assert.hasText(findBy, "findBy不能为空");
        Assert.hasText(value, "value不能为空");
        Assert.hasText(maxWaitTimeInSeconds, "最大等待时间不能为空");

        return new WebDriverWait(driver, Long.parseLong(maxWaitTimeInSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(getBy(findBy, value)));
    }

    /**
     * platform: Android / iOS
     * 13.切换context
     *
     * @param context
     */
    public void switchContext(String context) {
        Assert.hasText(context, "context不能为空");
        driver.context(context);
    }

    /**
     * 14.休眠
     * platform: Android / iOS
     *
     * @param sleepTimeInSeconds
     * @throws InterruptedException
     */
    public void sleep(String sleepTimeInSeconds) throws InterruptedException {
        Assert.hasText(sleepTimeInSeconds, "休眠时长不能为空");
        long sleepTime = (long) (Float.parseFloat(sleepTimeInSeconds) * 1000);
        Thread.sleep(sleepTime);
    }

    /**
     * 15.滑动屏幕
     * platform: Android / iOS
     *
     * @param startPoint {"x": 0.5, "y": 0.5} => 屏幕宽/高 1/2的位置
     * @param endPoint
     */
    public void swipeInScreen(String startPoint, String endPoint) {
        JSONObject _startPoint;
        JSONObject _endPoint;
        try {
            _startPoint = JSON.parseObject(startPoint);
            _endPoint = JSON.parseObject(endPoint);
        } catch (Exception e) {
            throw new RuntimeException("startPoint,endPoint格式错误，正确格式: {x:0.5,y:0.5}");
        }

        int startX = (int) (_startPoint.getFloat("x") * screenWidth);
        int startY = (int) (_startPoint.getFloat("y") * screenHeight);
        int endX = (int) (_endPoint.getFloat("x") * screenWidth);
        int endY = (int) (_endPoint.getFloat("y") * screenHeight);

        log.info("滑动屏幕: ({},{}) -> ({},{})", startX, startY, endX, endY);
        new TouchAction(driver)
                .press(PointOption.point(startX, startY))
                .waitAction(WaitOptions.waitOptions(Duration.ZERO))
                .moveTo(PointOption.point(endX, endY))
                .release()
                .perform();
    }

    /**
     * 16.滑动屏幕查找元素
     * platform: Android / iOS
     *
     * @param findBy
     * @param value
     * @param startPoint    {"x": 0.5, "y": 0.5} => 屏幕宽/高 1/2的位置
     * @param endPoint
     * @param maxSwipeCount
     * @return
     */
    public WebElement swipeInScreenAndFindElement(String findBy, String value, String startPoint, String endPoint, String maxSwipeCount) {
        Assert.hasText(findBy, "findBy不能为空");
        Assert.hasText(value, "value不能为空");
        Assert.hasText(maxSwipeCount, "最大滑动次数不能为空");

        By by = getBy(findBy, value);

        try {
            return driver.findElement(by);
        } catch (Exception e) {
        }

        int _maxSwipeCount = Integer.parseInt(maxSwipeCount);

        for (int i = 0; i < _maxSwipeCount; i++) {
            log.info("开始滑动第{}次屏幕", i + 1);
            swipeInScreen(startPoint, endPoint);
            try {
                return driver.findElement(by);
            } catch (Exception e) {
            }
        }

        return driver.findElement(by);
    }

    /**
     * 17.在容器元素内滑动
     * platform: Android / iOS
     *
     * @param container
     * @param startPoint {"x": 0.5, "y": 0.5} => 容器元素宽/高 1/2的位置
     * @param endPoint
     */
    public void swipeInContainerElement(WebElement container, String startPoint, String endPoint) {
        Assert.notNull(container, "容器元素不能为空");

        JSONObject _startPoint;
        JSONObject _endPoint;
        try {
            _startPoint = JSON.parseObject(startPoint);
            _endPoint = JSON.parseObject(endPoint);
        } catch (Exception e) {
            throw new RuntimeException("startPoint,endPoint格式错误，正确格式: {x:0.5,y:0.5}");
        }

        Rectangle containerRect = container.getRect();
        int containerHeight = containerRect.getHeight();
        int containerWidth = containerRect.getWidth();
        log.info("containerElement rect: {}", JSON.toJSONString(containerRect));

        // 左上角坐标
        Point containerRectPoint = containerRect.getPoint();
        int containerLeftTopX = containerRectPoint.getX();
        int containerLeftTopY = containerRectPoint.getY();

        int startX = containerLeftTopX + (int) (_startPoint.getFloat("x") * containerWidth);
        int startY = containerLeftTopY + (int) (_startPoint.getFloat("y") * containerHeight);
        int endX = containerLeftTopX + (int) (_endPoint.getFloat("x") * containerWidth);
        int endY = containerLeftTopY + (int) (_endPoint.getFloat("y") * containerHeight);

        log.info("在containerElement内滑动: ({},{}) -> ({},{})", startX, startY, endX, endY);
        new TouchAction(driver)
                .press(PointOption.point(startX, startY))
                .waitAction(WaitOptions.waitOptions(Duration.ZERO))
                .moveTo(PointOption.point(endX, endY))
                .release()
                .perform();
    }

    /**
     * 18.在容器元素内滑动查找元素
     * platform: Android / iOS
     *
     * @param container
     * @param findBy
     * @param value
     * @param startPoint    {"x": 0.5, "y": 0.5} => 容器元素宽/高 1/2的位置
     * @param endPoint
     * @param maxSwipeCount
     * @return
     */
    public WebElement swipeInContainerElementAndFindElement(WebElement container, String findBy, String value, String startPoint, String endPoint, String maxSwipeCount) {
        Assert.notNull(container, "容器元素不能为空");
        Assert.hasText(findBy, "findBy不能为空");
        Assert.hasText(value, "value不能为空");
        Assert.hasText(maxSwipeCount, "最大滑动次数不能为空");

        By by = getBy(findBy, value);
        try {
            return driver.findElement(by);
        } catch (Exception e) {
        }

        Rectangle containerRect = container.getRect();
        int containerHeight = containerRect.getHeight();
        int containerWidth = containerRect.getWidth();
        log.info("containerElement rect: {}", JSON.toJSONString(containerRect));

        // 左上角坐标
        Point containerRectPoint = containerRect.getPoint();
        int containerLeftTopX = containerRectPoint.getX();
        int containerLeftTopY = containerRectPoint.getY();

        JSONObject _startPoint;
        JSONObject _endPoint;
        try {
            _startPoint = JSON.parseObject(startPoint);
            _endPoint = JSON.parseObject(endPoint);
        } catch (Exception e) {
            throw new RuntimeException("startPoint,endPoint格式错误，正确格式: {x:0.5,y:0.5}");
        }

        int startX = containerLeftTopX + (int) (_startPoint.getFloat("x") * containerWidth);
        int startY = containerLeftTopY + (int) (_startPoint.getFloat("y") * containerHeight);
        int endX = containerLeftTopX + (int) (_endPoint.getFloat("x") * containerWidth);
        int endY = containerLeftTopY + (int) (_endPoint.getFloat("y") * containerHeight);

        int _maxSwipeCount = Integer.parseInt(maxSwipeCount);

        for (int i = 0; i < _maxSwipeCount; i++) {
            log.info("在containerElement内滑动第{}次: ({},{}) -> ({},{})", i + 1, startX, startY, endX, endY);
            new TouchAction(driver)
                    .press(PointOption.point(startX, startY))
                    .waitAction(WaitOptions.waitOptions(Duration.ZERO))
                    .moveTo(PointOption.point(endX, endY))
                    .release()
                    .perform();
            try {
                return driver.findElement(by);
            } catch (Exception e) {
            }
        }

        return driver.findElement(by);
    }

    /**
     * platform: Android / iOS
     * 19.切换窗口
     */
    public void switchWindow(String window) {
        driver.switchTo().window(window);
    }

    private By getBy(String findBy, String value) {
        By by;
        switch (findBy) {
            case "id":
                by = MobileBy.id(value);
                break;
            case "AccessibilityId":
                by = MobileBy.AccessibilityId(value);
                break;
            case "xpath":
                by = MobileBy.xpath(value);
                break;
            case "AndroidUIAutomator":
                // http://appium.io/docs/en/writing-running-appium/android/uiautomator-uiselector/
                value = value.replaceAll("'", "\"");
                by = MobileBy.AndroidUIAutomator(value);
                break;
            case "iOSClassChain":
                by = MobileBy.iOSClassChain(value);
                break;
            case "iOSNsPredicateString":
                // http://appium.io/docs/en/writing-running-appium/ios/ios-predicate/
                by = MobileBy.iOSNsPredicateString(value);
                break;
            case "image":
                by = MobileBy.image(value);
                break;
            case "className":
                by = MobileBy.className(value);
                break;
            case "name":
                by = MobileBy.name(value);
                break;
            case "cssSelector":
                by = MobileBy.cssSelector(value);
                break;
            case "linkText":
                by = MobileBy.linkText(value);
                break;
            case "partialLinkText":
                by = MobileBy.partialLinkText(value);
                break;
            case "tagName":
                by = MobileBy.tagName(value);
                break;
            default:
                throw new RuntimeException("暂不支持: " + findBy);
        }
        return by;
    }

}
