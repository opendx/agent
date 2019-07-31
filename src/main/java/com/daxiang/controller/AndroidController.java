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

    /**
     * 开启adbkit
     *
     * @param deviceId
     * @return
     */
    @GetMapping("/{deviceId}/adbkit/start")
    public Response startAdbKit(@PathVariable String deviceId) {
        return androidService.startAdbKit(deviceId);
    }

    /**
     * 停止adbkit
     *
     * @param deviceId
     * @return
     */
    @GetMapping("/{deviceId}/adbkit/stop")
    public Response stop(@PathVariable String deviceId) {
        return androidService.stopAdbKit(deviceId);
    }

    /**
     * aaptDumpBadging
     * @param apkDownloadUrl
     * @return
     */
    @PostMapping("/aaptDumpBadging")
    public Response aaptDumpBadging(@RequestBody String apkDownloadUrl) {
        return androidService.aaptDumpBadging(apkDownloadUrl);
    }
}