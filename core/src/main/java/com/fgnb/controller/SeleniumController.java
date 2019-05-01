package com.fgnb.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fgnb.init.SeleniumInitializer;
import com.fgnb.model.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/selenium")
public class SeleniumController {

    @GetMapping("/getDrivers")
    public Response getDrivers(){
        JSONArray result = new JSONArray();

        JSONObject chrome = new JSONObject();
        chrome.put("type",1);
        chrome.put("name","chrome");
        chrome.put("port",SeleniumInitializer.getChromeDriverServicePort());

        result.add(chrome);
        return Response.success(result);
    }
}
