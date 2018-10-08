package com.fgnb.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.util.Map;

/**
 * Created by jiangyitao.
 */
public class JsonUtil {

    public static Map objectToMap(Object obj){
        return stringToMap(JSON.toJSONString(obj));
    }

    public static Map stringToMap(String str){
        return JSON.parseObject(str,new TypeReference<Map<String, Object>>(){} );
    }

    public static JSONObject stringToJson(String jsonString){
       return JSON.parseObject(jsonString);
    }

    public static String objectToString(Object o){
        return JSON.toJSONString(o);
    }

}
