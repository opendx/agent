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

    @GetMapping("/{mobileId}/adbkit/start")
    public Response startAdbKit(@PathVariable String mobileId) {
        return androidService.startAdbKit(mobileId);
    }

    @GetMapping("/{mobileId}/adbkit/stop")
    public Response stop(@PathVariable String mobileId) {
        return androidService.stopAdbKit(mobileId);
    }

    @PostMapping("/aaptDumpBadging")
    public Response aaptDumpBadging(@RequestBody String apkDownloadUrl) {
        return androidService.aaptDumpBadging(apkDownloadUrl);
    }

    @GetMapping("{mobileId}/imeList")
    public Response getImeList(@PathVariable String mobileId) {
        return androidService.getImeList(mobileId);
    }

    @PostMapping("{mobileId}/ime")
    public Response setIme(@PathVariable String mobileId, String ime) {
        return androidService.setIme(mobileId, ime);
    }
}