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

        int count = StringUtils.hasText(maxSwipeCount) ? parseInt(maxSwipeCount) : 3;

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

        int count = StringUtils.hasText(maxSwipeCount) ? parseInt(maxSwipeCount) : 3;

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
     * // todo 转移到BaseAction
     */
    public boolean acceptAlert() {
        return device.acceptAlert();
    }

    /**
     * 1008.异步accept alert
     *
     * @param timeoutInSeconds 超时处理时间
     * @param once             是否只处理一次
     *                         // todo 转移到BaseAction
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
     * // todo 转移到BaseAction
     */
    public boolean dismissAlert() {
        return device.dismissAlert();
    }

    /**
     * 1010.异步dismiss alert
     *
     * @param timeoutInSeconds 超时处理时间
     * @param once             是否只处理一次
     *                         // todo 转移到BaseAction
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

    /**
     * 1011.通过TouchAction点击
     */
    public WebElement clickByTouchAction(String findBy, String value) {
        WebElement element = findElement(findBy, value);
        PointOption center = getElementCenter(element);

        new TouchAction(getAppiumDriver()).tap(center).perform();
        return element;
    }

    /**
     * 1012.长按元素
     */
    public void longPressElement(WebElement element, String durationInMs) {
        Assert.hasText(durationInMs, "durationInMs不能为空");

        PointOption center = getElementCenter(element);
        new TouchAction(getAppiumDriver())
                .longPress(center)
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(parseLong(durationInMs))))
                .release().perform();
    }

    private PointOption getElementCenter(WebElement element) {
        Assert.notNull(element, "element不能为空");

        Point leftTopPoint = element.getLocation();
        Dimension dimension = element.getSize();

        int x = leftTopPoint.x + dimension.width / 2;
        int y = leftTopPoint.y + dimension.height / 2;
        return PointOption.point(x, y);
    }

    private void swipe(Point start, Point end, String durationInMs) {
        long duration = StringUtils.hasText(durationInMs) ? parseLong(durationInMs) : DEFAULT_SWIPE_DURATION_IN_MS;
        new TouchAction(getAppiumDriver())
                .press(PointOption.point(start))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(duration)))
                .moveTo(PointOption.point(end))
                .release()
                .perform();
    }

    private Point createPoint(String point, Dimension window) {
        try {
            JSONObject _point = JSONObject.parseObject(point.trim());
            int x = (int) (_point.getFloat("x") * window.width);
            int y = (int) (_point.getFloat("y") * window.height);

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
