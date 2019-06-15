package com.daxiang.actions.utils;

import com.alibaba.fastjson.JSONObject;
import macaca.client.MacacaClient;
import macaca.client.commands.Element;

/**
 * Created by jiangyitao.
 */
public class MacacaUtil {

    public static MacacaClient createDriver(String deviceId, int port) throws Exception {
        JSONObject props = new JSONObject();
        props.put("host", "localhost");
        props.put("port", port);

        JSONObject desiredCapabilities = new JSONObject();
        desiredCapabilities.put("desiredCapabilities", props);

        MacacaClient driver = new MacacaClient().initDriver(desiredCapabilities);

        JSONObject deviceIdJSONObject = new JSONObject();
        deviceIdJSONObject.put("deviceId", deviceId);
        driver.contexts.setCapabilities(deviceIdJSONObject);

        return driver;
    }

    public static String getDeviceId(MacacaClient driver) {
        return (String) driver.contexts.getCapabilities().get("deviceId");
    }

    public static Element waitForElement(MacacaClient driver, String findBy, String value, long timeoutOfMillisecond) throws Exception {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime > timeoutOfMillisecond) {
                throw new RuntimeException("【 " + findBy + " ->" + value + " 】" + "元素不存在");
            }
            try {
                Element element = driver.element(value, findBy);
                if (element != null) {
                    return element;
                }
            } catch (Exception e) {
                //ignore
            }
        }
    }
}
