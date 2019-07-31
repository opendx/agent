package com.daxiang.controller;

import com.daxiang.model.Response;
import com.daxiang.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/mobile")
public class MobileController {

    @Autowired
    private MobileService mobileService;

    // todo
    @PostMapping("/{deviceId}/installApp")
    public Response installApp(MultipartFile app, @PathVariable String deviceId) {
        return mobileService.installApp()
    }
}
