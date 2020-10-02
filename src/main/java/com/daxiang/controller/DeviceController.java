package com.daxiang.controller;

import com.daxiang.model.Response;
import com.daxiang.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/{deviceId}/dump")
    public Response dump(@PathVariable String deviceId) {
        return Response.success(deviceService.dump(deviceId));
    }

    @GetMapping("/{deviceId}/screenshot")
    public Response screenshot(@PathVariable String deviceId) {
        return Response.success(deviceService.screenshot(deviceId));
    }

}
