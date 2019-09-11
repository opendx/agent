package com.daxiang.core.ios;

import com.daxiang.utils.Terminal;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class IosUtil {

    public static List<String> getDeviceList() {
        try {
            String result = Terminal.execute("idevice_id -l");
            if (StringUtils.isEmpty(result)) {
                return Collections.emptyList();
            }
            return Arrays.asList(result.split("\n"));
        } catch (IOException e) {
            log.error("execute 'idevice_id -l' err", e);
            return Collections.emptyList();
        }
    }

    /**
     * @param deviceId
     * @return eg. 10.3.4
     * @throws IOException
     */
    public static String getSystemVersion(String deviceId) throws IOException {
        return Terminal.execute("ideviceinfo -k ProductVersion -u " + deviceId);
    }

    /**
     * @param deviceId
     * @return eg. iPhone5,2
     * @throws IOException
     */
    public static String getProductType(String deviceId) throws IOException {
        return Terminal.execute("ideviceinfo -k ProductType -u " + deviceId);
    }

    public static String getDeviceName(String deviceId) throws IOException {
        return Terminal.execute("ideviceinfo -k DeviceName -u " + deviceId);
    }

    public static File screenshotByIdeviceScreenshot(String deviceId) throws IOException {
        // Screenshot saved to screenshot-xxx.png
        String result = Terminal.execute("idevicescreenshot -u " + deviceId);
        if (StringUtils.isEmpty(result)) {
            throw new RuntimeException("截图失败，idevicescreenshot返回空");
        }
        String[] resultArr = result.split(" ");
        return new File(resultArr[resultArr.length - 1].replaceAll("\n", ""));
    }

    // http://appium.io/docs/en/commands/mobile-command/
    public static void pressHome(AppiumDriver appiumDriver) {
        appiumDriver.executeScript("mobile:pressButton", ImmutableMap.of("name", "home"));
    }

    public static void installIpa(String ipaPath, String deviceId) throws IOException {
        Terminal.execute("ideviceinstaller -i " + ipaPath + " -u " + deviceId);
    }

    // http://appium.io/docs/en/writing-running-appium/ios/ios-xctest-mobile-apps-management/index.html
    // make sure that terminateApp has been called first, otherwise WebDriverAgent will detect the state as a potential crash of the application.
    public static void installApp(AppiumDriver appiumDriver, String appDownloadUrl) {
        appiumDriver.executeScript("mobile: installApp", ImmutableMap.of("app", appDownloadUrl));
    }

    public static void uninstallApp(AppiumDriver appiumDriver, String bundleId) {
        appiumDriver.executeScript("mobile: removeApp", ImmutableMap.of("bundleId", bundleId));
    }

    public static void launchApp(AppiumDriver appiumDriver, String bundleId) {
        appiumDriver.executeScript("mobile: launchApp", ImmutableMap.of("bundleId", bundleId));
    }

    // Terminates an existing application on the device. If the application is not running then the returned result will be false, otherwise true
    public static boolean terminateApp(AppiumDriver appiumDriver, String bundleId) {
        return (Boolean) appiumDriver.executeScript("mobile: terminateApp", ImmutableMap.of("bundleId", bundleId));
    }
}