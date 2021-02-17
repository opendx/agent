package com.daxiang.action;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.action.annotation.Action;
import com.daxiang.core.action.annotation.Param;
import com.daxiang.core.mobile.MobileDevice;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.util.Assert;
import java.time.Duration;

/**
 * Created by jiangyitao.
 * id 1000 - 1999
 * platforms = [1,2]
 */
@Slf4j
public class MobileAction extends BaseAction {

    private static final long DEFAULT_SWIPE_DURATION_IN_MS = 100L;
    public static final String POINT_POSSIBLE_VALUES = "[" +
            "{'value':'{x:0.5, y:0.5}','description':'屏幕中心点'}," +
            "{'value':'{x:0.8, y:0.5}','description':''}," +
            "{'value':'{x:0.2, y:0.5}','description':''}," +
            "{'value':'{x:0.5, y:0.8}','description':''}," +
            "{'value':'{x:0.5, y:0.2}','description':''}," +
            "]";

    private MobileDevice mobileDevice;

    public MobileAction(MobileDevice mobileDevice) {
        super(mobileDevice);
        this.mobileDevice = mobileDevice;
    }

    private AppiumDriver getAppiumDriver() {
        return (AppiumDriver) device.getDriver();
    }

    @Action(id = 1000, name = "切换context", platforms = {1, 2})
    public void switchContext(@Param(possibleValues = "[{'value': 'NATIVE_APP', 'description': '原生'}]") String context) {
        Assert.hasText(context, "context不能为空");
        getAppiumDriver().context(context);
    }

    @Action(id = 1001, name = "安装App", platforms = {1, 2})
    public void installApp(@Param(description = "本地路径 or 下载链接") String app) {
        Assert.hasText(app, "app不能为空");
        mobileDevice.installApp(app);
    }

    @Action(id = 1002, name = "卸载App", platforms = {1, 2})
    public void uninstallApp(@Param(description = "android: packageName, iOS: bundleId") String app) throws Exception {
        Assert.hasText(app, "apk包名或ios bundleId不能为空");
        mobileDevice.uninstallApp(app);
    }

    @Action(id = 1003, name = "滑动屏幕", platforms = {1, 2})
    public void swipe(@Param(description = "起点", possibleValues = POINT_POSSIBLE_VALUES) String startPoint,
                      @Param(description = "终点", possibleValues = POINT_POSSIBLE_VALUES) String endPoint,
                      @Param(description = "滑动时间，单位：ms。时间越短，滑的距离越长") long durationInMs) {
        Dimension window = getAppiumDriver().manage().window().getSize();
        Point start = createPoint(startPoint, window);
        Point end = createPoint(endPoint, window);

        swipe(start, end, durationInMs);
    }

    @Action(id = 1004, name = "滑动屏幕查找元素", platforms = {1, 2})
    public WebElement swipeToFindElement(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy,
                                         String value,
                                         @Param(description = "起点", possibleValues = POINT_POSSIBLE_VALUES) String startPoint,
                                         @Param(description = "终点", possibleValues = POINT_POSSIBLE_VALUES) String endPoint,
                                         @Param(description = "最大滑动次数") int maxSwipeCount,
                                         @Param(description = "滑动一次的时间，单位: ms。时间越短，滑的距离越长") long onceDurationInMs) {
        By by = createBy(findBy, value);
        AppiumDriver driver = getAppiumDriver();
        try {
            return driver.findElement(by);
        } catch (Exception ign) {
        }

        Dimension window = driver.manage().window().getSize();
        Point start = createPoint(startPoint, window);
        Point end = createPoint(endPoint, window);

        for (int i = 1; i <= maxSwipeCount; i++) {
            log.info("[{}]滑动第{}次", mobileDevice.getId(), i);
            swipe(start, end, onceDurationInMs);
            try {
                return driver.findElement(by);
            } catch (Exception ign) {
            }
        }

        return driver.findElement(by);
    }

    @Action(id = 1005, name = "容器内滑动", platforms = {1, 2})
    public void swipeInContainer(@Param(description = "容器元素") WebElement container,
                                 @Param(description = "起点", possibleValues = POINT_POSSIBLE_VALUES) String startPoint,
                                 @Param(description = "终点", possibleValues = POINT_POSSIBLE_VALUES) String endPoint,
                                 @Param(description = "滑动一次的时间，单位: ms。时间越短，滑的距离越长") long onceDurationInMs) {
        Point[] points = createStartAndEndPointInContainer(container, startPoint, endPoint);
        swipe(points[0], points[1], onceDurationInMs);
    }

    @Action(id = 1006, name = "容器内滑动查找元素", platforms = {1, 2})
    public WebElement swipeInContainerToFindElement(@Param(description = "容器元素") WebElement container,
                                                    @Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy,
                                                    String value,
                                                    @Param(description = "起点", possibleValues = POINT_POSSIBLE_VALUES) String startPoint,
                                                    @Param(description = "终点", possibleValues = POINT_POSSIBLE_VALUES) String endPoint,
                                                    @Param(description = "最大滑动次数") int maxSwipeCount,
                                                    @Param(description = "滑动一次的时间，单位: ms。时间越短，滑的距离越长") long onceDurationInMs) {
        By by = createBy(findBy, value);
        AppiumDriver driver = getAppiumDriver();
        try {
            return driver.findElement(by);
        } catch (Exception ign) {
        }

        Point[] points = createStartAndEndPointInContainer(container, startPoint, endPoint);

        for (int i = 1; i <= maxSwipeCount; i++) {
            log.info("[{}]容器内滑动第{}次", mobileDevice.getId(), i);
            swipe(points[0], points[1], onceDurationInMs);
            try {
                return driver.findElement(by);
            } catch (Exception e) {
            }
        }

        return driver.findElement(by);
    }

    @Deprecated
    @Action(id = 1007, name = "accept对话框", platforms = {1, 2})
    public boolean acceptAlert() {
        return super.acceptAlert();
    }

    @Deprecated
    @Action(id = 1008, name = "异步accept对话框", platforms = {1, 2})
    public void asyncAcceptAlert(@Param(description = "超时时间，单位：秒") long timeoutInSeconds,
                                 @Param(description = "是否只处理一次, true or false") boolean once) {
        super.asyncAcceptAlert(timeoutInSeconds, once);
    }

    @Deprecated
    @Action(id = 1009, name = "dismiss对话框", platforms = {1, 2})
    public boolean dismissAlert() {
        return super.dismissAlert();
    }

    @Deprecated
    @Action(id = 1010, name = "异步dismiss对话框", platforms = {1, 2})
    public void asyncDismissAlert(@Param(description = "超时时间，单位：秒") long timeoutInSeconds,
                                  @Param(description = "是否只处理一次, true or false") boolean once) {
        super.asyncDismissAlert(timeoutInSeconds, once);
    }

    @Action(id = 1011, name = "(TouchAction)点击", platforms = {1, 2})
    public WebElement clickByTouchAction(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value) {
        WebElement element = findElement(findBy, value);
        PointOption center = getElementCenter(element);

        new TouchAction(getAppiumDriver()).tap(center).perform();
        return element;
    }

    @Action(id = 1012, name = "长按元素", platforms = {1, 2})
    public void longPressElement(WebElement element, @Param(description = "长按时间，单位：ms") long durationInMs) {
        PointOption center = getElementCenter(element);
        new TouchAction(getAppiumDriver())
                .longPress(center)
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(durationInMs)))
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

    private void swipe(Point start, Point end, long durationInMs) {
        if (durationInMs == 0) {
            durationInMs = DEFAULT_SWIPE_DURATION_IN_MS;
        }
        new TouchAction(getAppiumDriver())
                .press(PointOption.point(start))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(durationInMs)))
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
