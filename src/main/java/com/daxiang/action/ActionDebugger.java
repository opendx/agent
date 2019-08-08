package com.daxiang.action;

import com.daxiang.utils.UUIDUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * Created by jiangyitao.
 */
public class ActionDebugger {

    /**
     * 专门用来调试action的地方
     * 1. 在平台上使用一台设备
     * 2. 将设备id填入下方final String deviceId = "{deviceId}";
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 0. 填写设备id
        final String deviceId = "76UBBL422MCZ";
        // 1. 读取DeveloperCode.java
        String code = FileUtils.readFileToString(
                new File("/Users/jiangyitao/workspace/IdeaProjects/src/main/java/com/daxiang/action/DeveloperCode.java"),
                "UTF-8");
        String className = "DeveloperCode_" + UUIDUtil.getUUID();
        code = code.replaceAll("DeveloperCode", className);
        code = code.replaceAll("DEVICE_ID", deviceId);

        // 2.发送到ActionController @PostMapping("/developer/debug")执行
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("className", className);
        params.add("code", code);
        String response = restTemplate.postForObject("http://192.168.1.8:10004/action/developer/debug", params, String.class);
        System.out.println("response => " + response);
    }
}
