package com.fgnb.controller;

import com.fgnb.model.Response;
import com.fgnb.service.Uiautomator2ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/uiautomator2server")
public class Uiautomator2ServerController {

    @Autowired
    private Uiautomator2ServerService uiautomator2ServerService;

    @GetMapping("/start/{deviceId}")
    public Response start(@PathVariable String deviceId) {
        return uiautomator2ServerService.start(deviceId);
    }
}
