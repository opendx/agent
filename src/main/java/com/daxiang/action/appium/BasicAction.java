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

import java.time.Duration;
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
    public void executeJavaCode(Object javaCode) {
        Assert.notNull(javaCode, "javaCode不能为空");
    }

    /**
     * 2.卸载app
     * platform: Android / iOS
     *
     * @param packageName
     * @throws Exception
     */
    public void uninstallApp(Object packageName) throws Exception {
        Assert.notNull(packageName, "apk包名或ios bundleId不能为空");
        String _packageName = (String) packageName;

        if (driver instanceof AndroidDriver) {
            AndroidUtil.uninstallApk(MobileDeviceHolder.getIDeviceByAppiumDriver(driver), _packageName);
        } else {
            IosUtil.uninstallApp(driver, _packageName);
        }
    }

    /**
     * 3.安装app
     * platform: Android / iOS
     *
     * @param appDownloadUrl
     * @throws Exception
     */
    public void installApp(Object appDownloadUrl) throws Exception {
        Assert.notNull(appDownloadUrl, "appDownloadUrl不能为空");
        String _appDownloadUrl = (String) appDownloadUrl;

        MobileDeviceHolder.getMobileDeviceByAppiumDriver(driver).installApp(_appDownloadUrl);
    }

    /**
     * platform: Android
     * 4.清除apk数据
     *
     * @param packageName
     * @throws Exception
     */
    public void clearApkData(Object packageName) throws Exception {
        Assert.notNull(packageName, "包名不能为空");

        AndroidUtil.clearApkData(MobileDeviceHolder.getIDeviceByAppiumDriver(driver), (String) packageName);
    }

    /**
     * platform: Android
     * 5.启动/重启 apk
     *
     * @param packageName
     * @param launchActivity
     * @throws Exception
     */
    public void restartApk(Object packageName, Object launchActivity) throws Exception {
        Assert.notNull(packageName, "包名不能为空");
        Assert.notNull(launchActivity, "启动Activity不能为空");

        AndroidUtil.restartApk(MobileDeviceHolder.getIDeviceByAppiumDriver(driver), (String) packageName, (String) launchActivity);
    }

    /**
     * platform: iOS
     * 6.启动/重启 app
     *
     * @param bundleId
     */
    public void restartIosApp(Object bundleId) {
        Assert.notNull(bundleId, "bundleId不能为空");
        String _bundleId = (String) bundleId;

        IosUtil.terminateApp(driver, _bundleId);
        IosUtil.launchApp(driver, _bundleId);
    }

    /**
     * platform: Android / iOS
     * 7.点击
     *
     * @param findBy
     * @param value
     * @return
     */
    public WebElement click(Object findBy, Object value) {
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
    public WebElement findElement(Object findBy, Object value) {
        Assert.notNull(findBy, "findBy不能为空");
        Assert.notNull(value, "value不能为空");

        return driver.findElement(getBy((String) findBy, (String) value));
    }

    /**
     * platform: Android / iOS
     * 9.查找元素
     *
     * @param findBy
     * @param value
     * @return 返回所有匹配的元素
     */
    public List<WebElement> findElements(Object findBy, Object value) {
        Assert.notNull(findBy, "findBy不能为空");
        Assert.notNull(value, "value不能为空");

        return driver.findElements(getBy((String) findBy, (String) value));
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
    public WebElement sendKeys(Object findBy, Object value, Object content) {
        Assert.notNull(findBy, "findBy不能为空");
        Assert.notNull(value, "value不能为空");
        Assert.notNull(content, "content不能为空");

        WebElement element = driver.findElement(getBy((String) findBy, (String) value));
        element.sendKeys((String) content);
        return element;
    }

    /**
     * platform: Android / iOS
     * 11.设置隐式等待时间
     *
     * @param implicitlyWaitTimeInSeconds
     */
    public void setImplicitlyWaitTime(Object implicitlyWaitTimeInSeconds) {
        Assert.notNull(implicitlyWaitTimeInSeconds, "隐式等待时间不能为空");

        driver.manage().timeouts().implicitlyWait(Long.parseLong((String) implicitlyWaitTimeInSeconds), TimeUnit.SECONDS);
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
    public WebElement waitForElementVisible(Object findBy, Object value, Object maxWaitTimeInSeconds) {
        Assert.notNull(findBy, "findBy不能为空");
        Assert.notNull(value, "value不能为空");
        Assert.notNull(maxWaitTimeInSeconds, "最大等待时间不能为空");

        return new WebDriverWait(driver, Long.parseLong((String) maxWaitTimeInSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(getBy((String) findBy, (String) value)));
    }

    /**
     * platform: Android / iOS
     * 13.切换context
     *
     * @param context
     * @return 切换后的context
     */
    public String switchContext(Object context) {
        Assert.notNull(context, "context不能为空");
        String _context = (String) context;

        if (MobileDevice.NATIVE_CONTEXT.equals(_context)) {
            // 切换到原生
            driver.context(_context);
        } else {
            Set<String> contexts = driver.getContextHandles();
            log.info("contexts: {}", contexts);
            for (String ctx : contexts) {
                // webview 目前先这样处理，如果有多个webview可能会切换错
                if (!MobileDevice.NATIVE_CONTEXT.equals(ctx)) {
                    driver.context(ctx);
                    break;
                }
            }
            _context = driver.getContext();
            if (MobileDevice.NATIVE_CONTEXT.equals(_context)) {
                throw new RuntimeException("未检测到webview，无法切换。当前contexts: " + contexts.toString());
            }
        }

        return _context;
    }

    /**
     * 14.休眠
     * platform: Android / iOS
     *
     * @param sleepTimeInSeconds
     * @throws InterruptedException
     */
    public void sleep(Object sleepTimeInSeconds) throws InterruptedException {
        Assert.notNull(sleepTimeInSeconds, "休眠时长不能为空");
        long sleepTime = (long) (Float.parseFloat((String) sleepTimeInSeconds) * 1000);
        Thread.sleep(sleepTime);
    }

    /**
     * 15.滑动屏幕
     * platform: Android / iOS
     *
     * @param startPoint {"x": 0.5, "y": 0.5} => 屏幕宽/高 1/2的位置
     * @param endPoint
     */
    public void swipeInScreen(Object startPoint, Object endPoint) {
        JSONObject _startPoint;
        JSONObject _endPoint;
        try {
            _startPoint = JSON.parseObject((String) startPoint);
            _endPoint = JSON.parseObject((String) endPoint);
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
    public WebElement swipeInScreenAndFindElement(Object findBy, Object value, Object startPoint, Object endPoint, Object maxSwipeCount) {
        Assert.notNull(findBy, "findBy不能为空");
        Assert.notNull(value, "value不能为空");
        Assert.notNull(maxSwipeCount, "最大滑动次数不能为空");

        By by = getBy((String) findBy, (String) value);

        try {
            return driver.findElement(by);
        } catch (Exception e) {
        }

        int _maxSwipeCount = Integer.parseInt((String) maxSwipeCount);

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
     * @param containerElement
     * @param startPoint       {"x": 0.5, "y": 0.5} => 容器元素宽/高 1/2的位置
     * @param endPoint
     */
    public void swipeInContainerElement(Object containerElement, Object startPoint, Object endPoint) {
        Assert.notNull(containerElement, "容器元素不能为空");

        JSONObject _startPoint;
        JSONObject _endPoint;
        try {
            _startPoint = JSON.parseObject((String) startPoint);
            _endPoint = JSON.parseObject((String) endPoint);
        } catch (Exception e) {
            throw new RuntimeException("startPoint,endPoint格式错误，正确格式: {x:0.5,y:0.5}");
        }

        WebElement container = (WebElement) containerElement;

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
     * @param containerElement
     * @param findBy
     * @param value
     * @param startPoint       {"x": 0.5, "y": 0.5} => 容器元素宽/高 1/2的位置
     * @param endPoint
     * @param maxSwipeCount
     * @return
     */
    public WebElement swipeInContainerElementAndFindElement(Object containerElement, Object findBy, Object value, Object startPoint, Object endPoint, Object maxSwipeCount) {
        Assert.notNull(containerElement, "容器元素不能为空");
        Assert.notNull(findBy, "findBy不能为空");
        Assert.notNull(value, "value不能为空");
        Assert.notNull(maxSwipeCount, "最大滑动次数不能为空");

        By by = getBy((String) findBy, (String) value);
        try {
            return driver.findElement(by);
        } catch (Exception e) {
        }

        WebElement container = (WebElement) containerElement;

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
            _startPoint = JSON.parseObject((String) startPoint);
            _endPoint = JSON.parseObject((String) endPoint);
        } catch (Exception e) {
            throw new RuntimeException("startPoint,endPoint格式错误，正确格式: {x:0.5,y:0.5}");
        }

        int startX = containerLeftTopX + (int) (_startPoint.getFloat("x") * containerWidth);
        int startY = containerLeftTopY + (int) (_startPoint.getFloat("y") * containerHeight);
        int endX = containerLeftTopX + (int) (_endPoint.getFloat("x") * containerWidth);
        int endY = containerLeftTopY + (int) (_endPoint.getFloat("y") * containerHeight);

        int _maxSwipeCount = Integer.parseInt((String) maxSwipeCount);

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
            default:
                throw new RuntimeException("暂不支持: " + findBy);
        }
        return by;
    }

}
