package com.daxiang.core.ios;

import com.daxiang.utils.ShellExecutor;
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
            String result = ShellExecutor.execute("idevice_id -l");
            if (StringUtils.isEmpty(result)) {
                return Collections.emptyList();
            }
            return Arrays.asList(result.split("\n"));
        } catch (IOException e) {
            log.error("excute 'idevice_id -l' err", e);
            return Collections.emptyList();
        }
    }

    /**
     * @param deviceId
     * @return eg. 10.3.4
     * @throws IOException
     */
    public static String getSystemVersion(String deviceId) throws IOException {
        return ShellExecutor.execute("ideviceinfo -k ProductVersion -u " + deviceId);
    }

    /**
     * @param deviceId
     * @return eg. iPhone5,2
     * @throws IOException
     */
    public static String getProductType(String deviceId) throws IOException {
        return ShellExecutor.execute("ideviceinfo -k ProductType -u " + deviceId);
    }

    public static String getDeviceName(String deviceId) throws IOException {
        return ShellExecutor.execute("ideviceinfo -k DeviceName -u " + deviceId);
    }

    public static File screenshotByIdeviceScreenshot(String deviceId) throws IOException {
        // Screenshot saved to screenshot-xxx.png
        String result = ShellExecutor.execute("idevicescreenshot -u " + deviceId);
        if (StringUtils.isEmpty(result)) {
            throw new RuntimeException("截图失败，idevicescreenshot返回：" + result);
        }
        String[] resultArr = result.split(" ");
        return new File(resultArr[resultArr.length - 1].replaceAll("\n", ""));
    }

    // http://appium.io/docs/en/commands/mobile-command/
    public static void pressHome(AppiumDriver appiumDriver) {
        appiumDriver.executeScript("mobile:pressButton", ImmutableMap.of("name", "home"));
    }

    public static void installIpa(String ipaPath, String deviceId) throws IOException {
        ShellExecutor.execute("ideviceinstaller -i " + ipaPath + " -u " + deviceId);
    }

}