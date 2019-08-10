package com.daxiang.action;

import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by jiangyitao.
 */
public class ActionDebugger {

    private AppiumDriver appiumDriver;

    @BeforeClass
    public void beforeClass() {
        appiumDriver = MobileDeviceHolder.get("DEVICE_ID").getAppiumDriver();
    }

    @Test
    public void test() {
        // 以下为要测试的代码
        String context = appiumDriver.getContext();
        System.out.println(context);
    }

    /**
     * 开发者调试代码
     * 1. 在平台上使用一台设备
     * 2. 将设备id，赋值给final String deviceId
     * 3. 运行main方法即可
     *
     * 提示: final String filePath / final String url 根据实际情况修改
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final String deviceId = "2dd931856e821196dc2a7e5358519d539c952e29";
        final String filePath = "/Users/jiangyitao/workspace/IdeaProjects/src/main/java/com/daxiang/action/ActionDebugger.java";
        final String url = "http://192.168.1.8:10004/action/developer/debug";

        // 1. 读取ActionDebugger.java
        String code = FileUtils.readFileToString(
                new File(filePath),
                "UTF-8");
        String className = "ActionDebugger_" + UUIDUtil.getUUID();
        code = code.replaceAll("ActionDebugger", className).replaceAll("DEVICE_ID", deviceId);

        // 2.发送到ActionController @PostMapping("/developer/debug")执行
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("className", "com.daxiang.action." + className);
        params.add("code", code);
        String response = restTemplate.postForObject(url, params, String.class);
        System.out.println("response => " + response);
    }
}
