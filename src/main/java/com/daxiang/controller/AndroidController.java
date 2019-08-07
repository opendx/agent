package com.daxiang.controller;

import com.daxiang.model.Response;
import com.daxiang.service.AndroidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/android")
public class AndroidController {

    @Autowired
    private AndroidService androidService;

    @GetMapping("/{deviceId}/adbkit/start")
    public Response startAdbKit(@PathVariable String deviceId) {
        return androidService.startAdbKit(deviceId);
    }

    @GetMapping("/{deviceId}/adbkit/stop")
    public Response stop(@PathVariable String deviceId) {
        return androidService.stopAdbKit(deviceId);
    }

    @PostMapping("/aaptDumpBadging")
    public Response aaptDumpBadging(@RequestBody String apkDownloadUrl) {
        return androidService.aaptDumpBadging(apkDownloadUrl);
    }
}