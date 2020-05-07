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

    @PostMapping("/{mobileId}/installApp")
    public Response installApp(MultipartFile app, @PathVariable String mobileId) {
        return mobileService.installApp(app, mobileId);
    }

    @GetMapping("/{mobileId}/dump")
    public Response dump(@PathVariable String mobileId) {
        return mobileService.dump(mobileId);
    }

    @GetMapping("/{mobileId}/screenshot")
    public Response screenshot(@PathVariable String mobileId) {
        return mobileService.screenshot(mobileId);
    }

    @GetMapping("/status")
    public Response status(String mobileId) {
        return mobileService.getStatus(mobileId);
    }
}
