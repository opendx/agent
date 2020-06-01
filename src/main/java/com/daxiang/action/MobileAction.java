package com.daxiang.action;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.utils.HttpUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

/**
 * Created by jiangyitao.
 * id 1000 - 1999
 * platforms = [1,2]
 */
@Slf4j
public class MobileAction extends BaseAction {

    private static final long DEFAULT_SWIPE_DURATION_IN_MS = 100L;

    private MobileDevice mobileDevice;

    public MobileAction(MobileDevice mobileDevice) {
        super(mobileDevice);
        this.mobileDevice = mobileDevice;
    }

    private AppiumDriver getAppiumDriver() {
        return (AppiumDriver) device.getDriver();
    }

    /**
     * 1000.切换context
     *
     * @param context
     */
    public void switchContext(String context) {
        Assert.hasText(context, "context不能为空");
        getAppiumDriver().context(context);
    }

    /**
     * 1001.安装app
     *
     * @param appDownloadUrl
     * @throws Exception
     */
    public void installApp(String appDownloadUrl) throws IOException {
        Assert.hasText(appDownloadUrl, "appDownloadUrl不能为空");

        File app = HttpUtil.downloadFile(appDownloadUrl);
        try {
            mobileDevice.installApp(app);
        } finally {
            FileUtils.deleteQuietly(app);
        }
    }

    /**
     * 1002.卸载app
     *
     * @param app
     * @throws Exception
     */
    public void uninstallApp(String app) throws Exception {
        Assert.hasText(app, "apk包名或ios bundleId不能为空");
        mobileDevice.uninstallApp(app);
    }

    /**
     * 1003.滑动屏幕
     *
     * @param startPoint   {x: 0.5, y: 0.5} => 屏幕宽/高 1/2的位置
     * @param endPoint
     * @param durationInMs 滑动耗时
     */
    public void swipe(String startPoint, String endPoint, String durationInMs) {
        Dimension window = getAppiumDriver().manage().window().getSize();
        Point start = createPoint(startPoint, window);
        Point end = createPoint(endPoint, window);

        swipe(start, end, durationInMs);
    }

    /**
     * 1004.滑动屏幕查找元素
     *
     * @param findBy
     * @param value
     * @param startPoint       {x: 0.5, y: 0.5} => 屏幕宽/高 1/2的位置
     * @param endPoint
     * @param maxSwipeCount    最大滑动次数
     * @param onceDurationInMs 滑动一次耗时
     * @return
     */
    public WebElement swipeToFindElement(String findBy, String value,
                                         String startPoint, String endPoint, String maxSwipeCount, String onceDurationInMs) {
        By by = createBy(findBy, value);
        AppiumDriver driver = getAppiumDriver();
        try {
            return driver.findElement(by);
        } catch (Exception ign) {
        }

        Dimension window = driver.manage().window().getSize();
        Point start = createPoint(startPoint, window);
        Point end = createPoint(endPoint, window);

        // 默认最多滑动3次
        if (!StringUtils.hasText(maxSwipeCount)) {
            maxSwipeCount = "3";
        }
        int count = parseInt(maxSwipeCount);

        for (int i = 1; i <= count; i++) {
            log.info("[{}]滑动第{}次", mobileDevice.getId(), i);
            swipe(start, end, onceDurationInMs);
            try {
                return driver.findElement(by);
            } catch (Exception ign) {
            }
        }

        return driver.findElement(by);
    }

    /**
     * 1005.在容器元素内滑动
     *
     * @param container
     * @param startPoint       {x: 0.5, y: 0.5} => 容器元素宽/高 1/2的位置
     * @param endPoint
     * @param onceDurationInMs 滑动一次耗时
     */
    public void swipeInContainer(WebElement container, String startPoint, String endPoint, String onceDurationInMs) {
        Point[] points = createStartAndEndPointInContainer(container, startPoint, endPoint);
        swipe(points[0], points[1], onceDurationInMs);
    }

    /**
     * 1006.在容器元素内滑动查找元素
     */
    public WebElement swipeInContainerToFindElement(WebElement container, String findBy, String value,
                                                    String startPoint, String endPoint, String maxSwipeCount, String onceDurationInMs) {
        By by = createBy(findBy, value);
        AppiumDriver driver = getAppiumDriver();
        try {
            return driver.findElement(by);
        } catch (Exception ign) {
        }

        Point[] points = createStartAndEndPointInContainer(container, startPoint, endPoint);

        // 默认最多滑动3次
        if (!StringUtils.hasText(maxSwipeCount)) {
            maxSwipeCount = "3";
        }
        int count = parseInt(maxSwipeCount);

        for (int i = 1; i <= count; i++) {
            log.info("[{}]容器内滑动第{}次", mobileDevice.getId(), i + 1);
            swipe(points[0], points[1], onceDurationInMs);
            try {
                return driver.findElement(by);
            } catch (Exception e) {
            }
        }

        return driver.findElement(by);
    }

    /**
     * 1007.弹窗 允许/接受/...
     */
    public boolean acceptAlert() {
        return mobileDevice.acceptAlert();
    }

    /**
     * 1008.异步accept alert
     *
     * @param timeoutInSeconds 超时处理时间
     * @param once             是否只处理一次
     */
    public void asyncAcceptAlert(String timeoutInSeconds, String once) {
        long _timeoutInSeconds = parseLong(timeoutInSeconds);
        boolean _once = parseBoolean(once);

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            boolean success;
            while (System.currentTimeMillis() - startTime < _timeoutInSeconds * 1000) {
                success = acceptAlert();
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
     * 1009.弹窗 拒绝/取消/...
     */
    public boolean dismissAlert() {
        return mobileDevice.dismissAlert();
    }

    /**
     * 1010.异步dismiss alert
     *
     * @param timeoutInSeconds 超时处理时间
     * @param once             是否只处理一次
     */
    public void asyncDismissAlert(String timeoutInSeconds, String once) {
        long _timeoutInSeconds = parseLong(timeoutInSeconds);
        boolean _once = parseBoolean(once);

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            boolean success;
            while (System.currentTimeMillis() - startTime < _timeoutInSeconds * 1000) {
                success = dismissAlert();
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

    private void swipe(Point start, Point end, String durationInMs) {
        long duration = DEFAULT_SWIPE_DURATION_IN_MS;
        if (!StringUtils.isEmpty(durationInMs)) {
            duration = parseLong(durationInMs);
        }

        new TouchAction(getAppiumDriver())
                .press(PointOption.point(start))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(duration)))
                .moveTo(PointOption.point(end))
                .release()
                .perform();
    }

    private Point createPoint(String point, Dimension window) {
        int screenWidth = window.width;
        int screenHeight = window.height;

        try {
            JSONObject _point = JSONObject.parseObject(point.trim());
            int x = (int) (_point.getFloat("x") * screenWidth);
            int y = (int) (_point.getFloat("y") * screenHeight);

            return new Point(x, y);
        } catch (Exception e) {
            throw new IllegalArgumentException(point + "格式错误, 正确格式示例: {x:0.5,y:0.5}");
        }
    }

    private Point[] createStartAndEndPointInContainer(WebElement container, String startPoint, String endPoint) {
        Assert.notNull(container, "container不能为空");

        Rectangle containerRect = container.getRect();
        int containerHeight = containerRect.getHeight();
        int containerWidth = containerRect.getWidth();

        // 容器左上角坐标
        Point containerRectPoint = containerRect.getPoint();
        int containerLeftTopX = containerRectPoint.getX();
        int containerLeftTopY = containerRectPoint.getY();

        try {
            JSONObject _startPoint = JSONObject.parseObject(startPoint.trim());
            JSONObject _endPoint = JSONObject.parseObject(endPoint.trim());

            int startX = containerLeftTopX + (int) (_startPoint.getFloat("x") * containerWidth);
            int startY = containerLeftTopY + (int) (_startPoint.getFloat("y") * containerHeight);
            int endX = containerLeftTopX + (int) (_endPoint.getFloat("x") * containerWidth);
            int endY = containerLeftTopY + (int) (_endPoint.getFloat("y") * containerHeight);

            return new Point[]{new Point(startX, startY), new Point(endX, endY)};
        } catch (Exception e) {
            throw new IllegalArgumentException("point格式错误，正确格式示例: {x:0.5,y:0.5}");
        }
    }

}
