package com.fgnb.controller;

import com.fgnb.enums.DriverType;
import com.fgnb.init.SeleniumInitializer;
import com.fgnb.vo.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/selenium")
public class SeleniumController {

    /**
     * @param driverType  chrome:1
     * @return
     */
    @GetMapping("/getPort")
    public Response getPort(Integer driverType){
        if(driverType == null){
            return Response.fail("driverType不能为空");
        }
        if(driverType == DriverType.CHROME.getType()){
            return Response.success("获取成功",SeleniumInitializer.getChromeDriverServicePort());
        }
        return Response.fail("driverType错误");
    }
}
