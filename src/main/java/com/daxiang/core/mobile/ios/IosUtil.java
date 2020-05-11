package com.daxiang.core.mobile.ios;

import com.daxiang.utils.Terminal;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class IosUtil {

    /**
     * 获取正在连接的真机列表
     *
     * @param showLog
     * @return
     */
    public static Set<String> getRealDeviceList(boolean showLog) {
        String cmd = "idevice_id -l";
        try {
            String cmdResponse = Terminal.execute(cmd, showLog);
            if (!StringUtils.hasText(cmdResponse)) {
                return Collections.emptySet();
            }

            return Arrays.asList(cmdResponse.split("\\r?\\n")).stream().collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("execute '{}' err", cmd, e);
            return Collections.emptySet();
        }
    }

    /**
     * 获取正在运行的模拟器列表
     *
     * @param showLog
     * @return
     */
    public static Set<String> getSimulatorList(boolean showLog) {
        String cmd = "xcrun simctl list devices |grep Booted";
        try {
            String cmdResponse = Terminal.execute(cmd, showLog);
            if (!StringUtils.hasText(cmdResponse)) {
                return Collections.emptySet();
            }

            Set<String> simulatorIds = new HashSet<>();

            Matcher matcher = Pattern.compile("\\w{8}(-\\w{4}){3}-\\w{12}").matcher(cmdResponse);
            while (matcher.find()) {
                simulatorIds.add(matcher.group());
            }

            return simulatorIds;
        } catch (Exception e) {
            log.error("execute '{}' err", cmd, e);
            return Collections.emptySet();
        }
    }

    public static String getRealDeviceSystemVersion(String mobileId) throws IOException {
        return Terminal.execute("ideviceinfo -k ProductVersion -u " + mobileId);
    }

    public static String getDeviceName(String mobileId, boolean isRealDevice) throws IOException {
        if (isRealDevice) {
            return Terminal.execute("ideviceinfo -k DeviceName -u " + mobileId);
        } else {
            String cmdResponse = Terminal.execute(String.format("xcrun simctl list devices |grep '%s'", mobileId));
            return cmdResponse.substring(0, cmdResponse.indexOf("(")).trim();
        }
    }

    public static void uninstallApp(RemoteWebDriver driver, String bundleId) {
        driver.executeScript("mobile: removeApp", ImmutableMap.of("bundleId", bundleId));
    }

    public static void launchApp(RemoteWebDriver driver, String bundleId) {
        driver.executeScript("mobile: launchApp", ImmutableMap.of("bundleId", bundleId));
    }

    // Terminates an existing application on the mobile. If the application is not running then the returned result will be false, otherwise true
    public static boolean terminateApp(RemoteWebDriver driver, String bundleId) {
        return (Boolean) driver.executeScript("mobile: terminateApp", ImmutableMap.of("bundleId", bundleId));
    }

    // http://appium.io/docs/en/commands/mobile-command/
    public static void pressHome(RemoteWebDriver driver) {
        driver.executeScript("mobile:pressButton", ImmutableMap.of("name", "home"));
    }
}