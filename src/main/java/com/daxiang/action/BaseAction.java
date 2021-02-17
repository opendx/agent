package com.daxiang.action;

import com.daxiang.core.action.annotation.Action;
import com.daxiang.core.action.annotation.Param;
import com.daxiang.core.Device;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.MobileBy;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 * id 1 - 999
 * platforms = null
 */
public class BaseAction {

    public static final int EXECUTE_JAVA_CODE_ID = 1;
    public static final String FIND_BY_POSSIBLE_VALUES = "[" +
            "{'value':'id','description':'By.id'}," +
            "{'value':'AccessibilityId','description':'By.AccessibilityId'}," +
            "{'value':'xpath','description':'By.xpath'}," +
            "{'value':'AndroidUIAutomator','description':'By.AndroidUIAutomator'}," +
            "{'value':'iOSClassChain','description':'By.iOSClassChain'}," +
            "{'value':'iOSNsPredicateString','description':'By.iOSNsPredicateString'}," +
            "{'value':'image','description':'By.image'}," +
            "{'value':'className','description':'By.className'}," +
            "{'value':'name','description':'By.name'}," +
            "{'value':'cssSelector','description':'By.cssSelector'}," +
            "{'value':'linkText','description':'By.linkText'}," +
            "{'value':'partialLinkText','description':'By.partialLinkText'}," +
            "{'value':'tagName','description':'By.tagName'}" +
            "]";

    protected Device device;

    public BaseAction(Device device) {
        this.device = device;
    }

    @Action(id = EXECUTE_JAVA_CODE_ID, name = "执行java代码")
    public void executeJavaCode(@Param(description = "java代码") String code) {
        Assert.hasText(code, "code不能为空");
    }

    @Action(id = 2, name = "休眠")
    public void sleep(@Param(description = "休眠时长，单位：毫秒") long ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    @Action(id = 3, name = "下载文件", returnValueDesc = "文件绝对路径")
    public String downloadFile(@Param(description = "下载文件") String url, @Param(description = "文件扩展名，如: jpg") String ext) throws IOException {
        String file = StringUtils.isEmpty(ext) ? UUIDUtil.getUUIDFilename(url) : UUIDUtil.getUUID() + "." + ext;
        File downloadFile = new File(file);
        FileUtils.copyURLToFile(new URL(url), downloadFile);
        return downloadFile.getAbsolutePath();
    }

    @Action(id = 4, name = "删除文件")
    public boolean deleteFileQuitely(@Param(description = "文件路径") String filePath) {
        return FileUtils.deleteQuietly(new File(filePath));
    }

    @Action(id = 7, name = "点击")
    public WebElement click(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value) {
        WebElement element = findElement(findBy, value);
        element.click();
        return element;
    }

    @Action(id = 8, name = "查找元素")
    public WebElement findElement(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value) {
        return device.getDriver().findElement(createBy(findBy, value));
    }

    @Action(id = 9, name = "查找元素列表", returnValueDesc = "查找到的元素列表")
    public List<WebElement> findElements(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value) {
        return device.getDriver().findElements(createBy(findBy, value));
    }

    @Action(id = 10, name = "输入")
    public WebElement sendKeys(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value,
                               @Param(description = "输入内容") String content) {
        WebElement element = device.getDriver().findElement(createBy(findBy, value));
        if (content == null) {
            content = "";
        }
        element.sendKeys(content);
        return element;
    }

    @Action(id = 11, name = "设置隐式等待时间")
    public void setImplicitlyWaitTime(@Param(description = "隐式等待时间，单位：秒") long seconds) {
        device.getDriver().manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
    }

    @Action(id = 12, name = "等待元素可见")
    public WebElement waitForElementVisible(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value,
                                            @Param(description = "最大等待时间，单位：秒") long timeoutInSeconds) {
        return new WebDriverWait(device.getDriver(), timeoutInSeconds)
                .until(ExpectedConditions.visibilityOfElementLocated(createBy(findBy, value)));
    }

    @Action(id = 13, name = "等待元素出现", description = "等待元素在DOM里出现，不一定可见。移动端可用于检测toast")
    public WebElement waitForElementPresence(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value,
                                             @Param(description = "最大等待时间，单位：秒") long timeoutInSeconds) {
        return new WebDriverWait(device.getDriver(), timeoutInSeconds)
                .until(ExpectedConditions.presenceOfElementLocated(createBy(findBy, value)));
    }

    @Action(id = 14, name = "[web]访问url")
    public void getUrl(String url) {
        Assert.hasText(url, "url不能为空");
        device.getDriver().get(url);
    }

    @Action(id = 15, name = "[web]切换窗口", returnValueDesc = "是否切换成功")
    public boolean switchWindow(@Param(description = "窗口Title") String windowTitle) {
        Assert.hasText(windowTitle, "窗口Title不能为空");

        RemoteWebDriver driver = device.getDriver();

        String currWindowHandle = null;
        try {
            currWindowHandle = driver.getWindowHandle();
        } catch (Exception e) {
            // 当前窗口可能已关闭
        }

        Set<String> windowHandles = driver.getWindowHandles();
        for (String windowHandle : windowHandles) {
            driver.switchTo().window(windowHandle);
            if (windowTitle.equals(driver.getTitle())) {
                return true;
            }
        }

        if (currWindowHandle != null) {
            // 没找到要切的窗口，切回原来的
            driver.switchTo().window(currWindowHandle);
        }
        return false;
    }

    @Action(id = 16, name = "[web]点击(By JS)", description = "当通过click方法点击元素，出现element not interactable错误，可以尝试用该方法点击")
    public Object clickByJs(WebElement element) {
        Assert.notNull(element, "element不能为空");
        return device.getDriver().executeScript("$(arguments[0]).click()", element);
    }

    @Action(id = 17, name = "元素是否显示")
    public boolean isElementDisplayed(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value) {
        try {
            return isElementDisplayed(findElement(findBy, value));
        } catch (Exception e) {
            return false;
        }
    }

    @Action(id = 18, name = "元素是否显示")
    public boolean isElementDisplayed(WebElement element) {
        Assert.notNull(element, "element不能为空");

        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    @Action(id = 19, name = "等待元素可见")
    public WebElement waitForElementVisible(WebElement element, @Param(description = "最大等待时间，单位：秒") long timeoutInSeconds) {
        return new WebDriverWait(device.getDriver(), timeoutInSeconds)
                .until(ExpectedConditions.visibilityOf(element));
    }

    @Action(id = 20, name = "获取当前时间")
    public String now(@Param(description = "默认yyyy-MM-dd HH:mm:ss") String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    public String now() {
        return now("yyyy-MM-dd HH:mm:ss");
    }

    @Action(id = 21, name = "accept对话框")
    public boolean acceptAlert() {
        return device.acceptAlert();
    }

    @Action(id = 22, name = "异步accept对话框")
    public void asyncAcceptAlert(@Param(description = "超时时间，单位：秒") long timeoutInSeconds,
                                 @Param(description = "是否只处理一次, true or false") boolean once) {
        long timeoutInMs = timeoutInSeconds * 1000;

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeoutInMs) {
                if (acceptAlert() && once) {
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
            }
        }).start();
    }

    @Action(id = 23, name = "dismiss对话框")
    public boolean dismissAlert() {
        return device.dismissAlert();
    }

    @Action(id = 24, name = "异步dismiss对话框")
    public void asyncDismissAlert(@Param(description = "超时时间，单位：秒") long timeoutInSeconds,
                                  @Param(description = "是否只处理一次, true or false") boolean once) {
        long timeoutInMs = timeoutInSeconds * 1000;

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeoutInMs) {
                if (dismissAlert() && once) {
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
            }
        }).start();
    }

    @Action(id = 25, name = "[web]切换frame")
    public void switchToFrameByIndex(int index) {
        device.getDriver().switchTo().frame(index);
    }

    @Action(id = 26, name = "[web]切换frame")
    public void switchToFrameByNameOrId(String nameOrId) {
        device.getDriver().switchTo().frame(nameOrId);
    }

    @Action(id = 27, name = "[web]切换到parentFrame")
    public void switchToParentFrame() {
        device.getDriver().switchTo().parentFrame();
    }

    @Action(id = 28, name = "[web]切换到defaultContent")
    public void switchToDefaultContent() {
        device.getDriver().switchTo().defaultContent();
    }

    public By createBy(String findBy, String value) {
        Assert.hasText(findBy, "findBy不能为空");
        Assert.hasText(value, "value不能为空");

        switch (findBy) {
            case "id":
                return MobileBy.id(value);
            case "AccessibilityId":
                return MobileBy.AccessibilityId(value);
            case "xpath":
                return MobileBy.xpath(value);
            case "AndroidUIAutomator":
                // http://appium.io/docs/en/writing-running-appium/android/uiautomator-uiselector/
                return MobileBy.AndroidUIAutomator(value);
            case "iOSClassChain":
                return MobileBy.iOSClassChain(value);
            case "iOSNsPredicateString":
                // http://appium.io/docs/en/writing-running-appium/ios/ios-predicate/
                return MobileBy.iOSNsPredicateString(value);
            case "image":
                return MobileBy.image(value);
            case "className":
                return MobileBy.className(value);
            case "name":
                return MobileBy.name(value);
            case "cssSelector":
                return MobileBy.cssSelector(value);
            case "linkText":
                return MobileBy.linkText(value);
            case "partialLinkText":
                return MobileBy.partialLinkText(value);
            case "tagName":
                return MobileBy.tagName(value);
            default:
                throw new IllegalArgumentException("暂不支持: " + findBy);
        }
    }

}
