package com.daxiang.core.ios;

import com.daxiang.utils.ShellExecutor;
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

    public static File screenshotByIdeviceScreenshot(String deviceId) throws IOException {
        // Screenshot saved to screenshot-xxx.png
        String result = ShellExecutor.execute("idevicescreenshot -u " + deviceId);
        if (StringUtils.isEmpty(result) || !result.endsWith(".png")) {
            throw new RuntimeException("截图失败，idevicescreenshot返回：" + result);
        }
        return new File(result.split(" ")[3]);
    }

}