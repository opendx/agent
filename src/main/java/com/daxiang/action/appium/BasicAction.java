package com.daxiang.action.appium;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.android.AndroidUtil;
import com.daxiang.core.ios.IosUtil;
import com.daxiang.utils.HttpUtil;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class BasicAction {

    public static final int EXECUTE_JAVA_CODE_ID = 1;

    private static final long DEFAULT_SWIPE_DURATION_MS = 100L;

    private MobileDevice mobileDevice;
    private AppiumDriver driver;

    public BasicAction(AppiumDriver driver) {
        this.driver = driver;
        this.mobileDevice = MobileDeviceHolder.getMobileDeviceByAppiumDriver(driver);
        if (mobileDevice == null) {
            throw new RuntimeException("设备不存在");
        }
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

        mobileDevice.uninstallApp(packageName);
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

        mobileDevice.installApp(HttpUtil.downloadFile(appDownloadUrl));
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

        AndroidUtil.clearApkData(((AndroidDevice) mobileDevice).getIDevice(), packageName);
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

        AndroidUtil.restartApk(((AndroidDevice) mobileDevice).getIDevice(), packageName, launchActivity);
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
    public void swipeInScreen(String startPoint, String endPoint, String durationInMsOfSwipeOneTime) {
        Dimension window = driver.manage().window().getSize();
        int width = window.width;
        int height = window.height;
        swipeInScreen(getPoint(startPoint, width, height),
                getPoint(endPoint, width, height),
                getDurationInMsOfSwipeOneTime(durationInMsOfSwipeOneTime));
    }

    private void swipeInScreen(Point start, Point end, Long durationInMsOfSwipeOneTime) {
        log.info("[{}]滑动: {} -> {}, duration: {} ms", mobileDevice.getId(), start, end, durationInMsOfSwipeOneTime);
        new TouchAction(driver)
                .press(PointOption.point(start))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(durationInMsOfSwipeOneTime)))
                .moveTo(PointOption.point(end))
                .release()
                .perform();
    }

    private Point getPoint(String point, int screenWidth, int screenHeight) {
        JSONObject _point;
        try {
            _point = JSON.parseObject(point.trim());
        } catch (Exception e) {
            throw new RuntimeException("格式错误, 正确格式: {x:0.5,y:0.5}");
        }
        int x = (int) (_point.getFloat("x") * screenWidth);
        int y = (int) (_point.getFloat("y") * screenHeight);
        return new Point(x, y);
    }

    private long getDurationInMsOfSwipeOneTime(String durationInMsOfSwipeOneTime) {
        long swipeDuration = DEFAULT_SWIPE_DURATION_MS;
        if (!StringUtils.isEmpty(durationInMsOfSwipeOneTime)) {
            swipeDuration = Long.parseLong(durationInMsOfSwipeOneTime);
        }
        return swipeDuration;
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
    public WebElement swipeInScreenAndFindElement(String findBy, String value, String startPoint, String endPoint, String maxSwipeCount, String durationInMsOfSwipeOneTime) {
        Assert.hasText(maxSwipeCount, "最大滑动次数不能为空");

        By by = getBy(findBy, value);

        try {
            return driver.findElement(by);
        } catch (Exception e) {
        }

        Dimension window = driver.manage().window().getSize();
        int width = window.width;
        int height = window.height;

        Point start = getPoint(startPoint, width, height);
        Point end = getPoint(endPoint, width, height);
        long swipeDuration = getDurationInMsOfSwipeOneTime(durationInMsOfSwipeOneTime);

        for (int i = 0; i < Integer.parseInt(maxSwipeCount); i++) {
            log.info("[{}]滑动第{}次", mobileDevice.getId(), i + 1);
            swipeInScreen(start, end, swipeDuration);
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
    public void swipeInContainerElement(WebElement container, String startPoint, String endPoint, String durationInMsOfSwipeOneTime) {
        ImmutableMap<String, Point> points = getStartPointAndEndPointInContainer(container, startPoint, endPoint);

        swipeInScreen(points.get("start"), points.get("end"), getDurationInMsOfSwipeOneTime(durationInMsOfSwipeOneTime));
    }

    private ImmutableMap<String, Point> getStartPointAndEndPointInContainer(WebElement container, String startPoint, String endPoint) {
        Assert.notNull(container, "容器元素不能为空");

        JSONObject _startPoint;
        JSONObject _endPoint;
        try {
            _startPoint = JSON.parseObject(startPoint.trim());
            _endPoint = JSON.parseObject(endPoint.trim());
        } catch (Exception e) {
            throw new RuntimeException("startPoint,endPoint格式错误，正确格式: {x:0.5,y:0.5}");
        }

        Rectangle containerRect = container.getRect();
        int containerHeight = containerRect.getHeight();
        int containerWidth = containerRect.getWidth();

        // 容器左上角坐标
        Point containerRectPoint = containerRect.getPoint();
        int containerLeftTopX = containerRectPoint.getX();
        int containerLeftTopY = containerRectPoint.getY();

        int startX = containerLeftTopX + (int) (_startPoint.getFloat("x") * containerWidth);
        int startY = containerLeftTopY + (int) (_startPoint.getFloat("y") * containerHeight);
        int endX = containerLeftTopX + (int) (_endPoint.getFloat("x") * containerWidth);
        int endY = containerLeftTopY + (int) (_endPoint.getFloat("y") * containerHeight);

        return ImmutableMap.of("start", new Point(startX, startY), "end", new Point(endX, endY));
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
    public WebElement swipeInContainerElementAndFindElement(WebElement container, String findBy, String value, String startPoint, String endPoint, String maxSwipeCount, String durationInMsOfSwipeOneTime) {
        Assert.hasText(maxSwipeCount, "最大滑动次数不能为空");

        By by = getBy(findBy, value);
        try {
            return driver.findElement(by);
        } catch (Exception e) {
        }

        ImmutableMap<String, Point> points = getStartPointAndEndPointInContainer(container, startPoint, endPoint);
        long swipeDuration = getDurationInMsOfSwipeOneTime(durationInMsOfSwipeOneTime);

        for (int i = 0; i < Integer.parseInt(maxSwipeCount); i++) {
            log.info("[{}]容器内滑动第{}次", mobileDevice.getId(), i + 1);
            swipeInScreen(points.get("start"), points.get("end"), swipeDuration);
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
        Assert.hasText(window, "window不能为空");
        driver.switchTo().window(window);
    }

    /**
     * platform: Android / iOS
     * 20.等待元素在DOM里出现，不一定可见
     * 可用于检查toast是否显示
     *
     * @param findBy
     * @param value
     * @param maxWaitTimeInSeconds
     * @return
     */
    public WebElement waitForElementPresence(String findBy, String value, String maxWaitTimeInSeconds) {
        Assert.hasText(maxWaitTimeInSeconds, "最大等待时间不能为空");

        return new WebDriverWait(driver, Long.parseLong(maxWaitTimeInSeconds))
                .until(ExpectedConditions.presenceOfElementLocated(getBy(findBy, value)));
    }

    /**
     * 21.弹窗 允许/接受/...
     */
    public boolean acceptAlert() {
        return mobileDevice.acceptAlert();
    }

    /**
     * 22.弹窗 拒绝/取消/...
     */
    public boolean dismissAlert() {
        return mobileDevice.dismissAlert();
    }

    /**
     * 23. 清除输入框
     *
     * @param findBy
     * @param value
     */
    public void clearInput(String findBy, String value) {
        findElement(findBy, value).clear();
    }

    /**
     * 24.异步accept alert
     *
     * @param timeoutInSeconds 超时处理时间
     * @param once     是否只处理一次
     */
    public void asyncAcceptAlert(String timeoutInSeconds, String once) {
        long _timeoutInSeconds;
        boolean _once;
        try {
            _timeoutInSeconds = Long.parseLong(timeoutInSeconds);
            _once = Boolean.parseBoolean(once);
        } catch (Exception e) {
            throw new RuntimeException("非法参数");
        }

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < _timeoutInSeconds * 1000) {
                boolean success = acceptAlert();
                if (_once && success) {
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
            }
        }).start();
    }

    /**
     * 25.异步dismiss alert
     *
     * @param timeoutInSeconds 超时处理时间
     * @param once     是否只处理一次
     */
    public void asyncDismissAlert(String timeoutInSeconds, String once) {
        long _timeoutInSeconds;
        boolean _once;
        try {
            _timeoutInSeconds = Long.parseLong(timeoutInSeconds);
            _once = Boolean.parseBoolean(once);
        } catch (Exception e) {
            throw new RuntimeException("非法参数");
        }

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < _timeoutInSeconds * 1000) {
                boolean success = dismissAlert();
                if (_once && success) {
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
            }
        }).start();
    }

    private By getBy(String findBy, String value) {
        Assert.hasText(findBy, "findBy不能为空");
        Assert.hasText(value, "value不能为空");

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
