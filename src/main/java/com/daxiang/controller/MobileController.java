package com.daxiang.controller;

import com.daxiang.model.Response;
import com.daxiang.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/mobile")
public class MobileController {

    @Autowired
    private MobileService mobileService;

    @PostMapping("/{deviceId}/installApp")
    public Response installApp(MultipartFile app, @PathVariable String deviceId) {
        return mobileService.installApp(app, deviceId);
    }

    @GetMapping("/{deviceId}/dump")
    public Response dump(@PathVariable String deviceId) {
        return mobileService.dump(deviceId);
    }

    @GetMapping("/{deviceId}/screenshot")
    public Response screenshot(@PathVariable String deviceId) {
        return mobileService.screenshot(deviceId);
    }
}
