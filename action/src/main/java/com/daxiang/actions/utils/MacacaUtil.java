package com.daxiang.actions.utils;

import com.alibaba.fastjson.JSONObject;
import macaca.client.MacacaClient;
import macaca.client.commands.Element;

/**
 * Created by jiangyitao.
 */
public class MacacaUtil {

    public static MacacaClient createMacacaClient(String deviceId,int port) throws Exception {

        MacacaClient macacaClient = new MacacaClient();

        JSONObject props = new JSONObject();
        props.put("host","localhost");
        props.put("port",port);

        JSONObject desiredCapabilities = new JSONObject();
        desiredCapabilities.put("desiredCapabilities",props);

        macacaClient = macacaClient.initDriver(desiredCapabilities);
        //设置udid 为了 adb操作  adb -s udid
        JSONObject deviceIdJSONObject = new JSONObject();
        deviceIdJSONObject.put("deviceId",deviceId);
        macacaClient.contexts.setCapabilities(deviceIdJSONObject);
        return macacaClient;
    }

    public static String getDeviceId(MacacaClient macacaClient){
        return (String) macacaClient.contexts.getCapabilities().get("deviceId");
    }

    public static Element waitForElement(MacacaClient macacaClient, String findBy, String value, long timeOutMs) throws Exception{
        long startTime = System.currentTimeMillis();
        while(true){
            if(System.currentTimeMillis() - startTime > timeOutMs){
                throw new RuntimeException("["+findBy+" ->"+value+"]"+"元素不存在");
            }
            try {
                Element element = macacaClient.element(value, findBy);
                if(element != null) {
                    return element;
                }
            } catch (Exception e) {
                //ignore
            }
        }
    }
}
