package com.fgnb.controller;

import com.fgnb.model.Response;
import com.fgnb.service.AdbKitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/adbkit")
public class AdbKitController {

    private AdbKitService adbKitService;

    @GetMapping("/start/{deviceId}")
    public Response start(@PathVariable String deviceId) {
        return adbKitService.start(deviceId);
    }
}
