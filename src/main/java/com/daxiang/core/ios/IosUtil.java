package com.daxiang.core.ios;

import com.daxiang.utils.Terminal;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class IosUtil {

    public static List<String> getDeviceList(boolean showLog) {
        try {
            String result = Terminal.execute("idevice_id -l", showLog);
            if (StringUtils.isEmpty(result)) {
                return Collections.emptyList();
            }
            return Arrays.asList(result.split("\\r?\\n"));
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

    // http://appium.io/docs/en/commands/mobile-command/
    public static void pressHome(AppiumDriver appiumDriver) {
        appiumDriver.executeScript("mobile:pressButton", ImmutableMap.of("name", "home"));
    }

    public static void installIpa(String ipaPath, String deviceId) throws IOException {
        Terminal.execute("ideviceinstaller -i " + ipaPath + " -u " + deviceId);
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