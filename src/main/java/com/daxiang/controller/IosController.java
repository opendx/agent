package com.daxiang.controller;

import com.daxiang.model.Response;
import com.daxiang.service.IosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/ios")
public class IosController {

    @Autowired
    private IosService iosService;

    @GetMapping("/{deviceId}/pressHome")
    public Response pressHome(@PathVariable String deviceId) {
        return iosService.pressHome(deviceId);
    }

}