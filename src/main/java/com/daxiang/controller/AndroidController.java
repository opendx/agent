package com.daxiang.controller;

import com.daxiang.model.Response;
import com.daxiang.service.AndroidService;
import com.google.common.collect.ImmutableMap;
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
        int port = androidService.startAdbKit(mobileId);
        return Response.success(ImmutableMap.of("port", port));
    }

    @GetMapping("/{mobileId}/adbkit/stop")
    public Response stop(@PathVariable String mobileId) {
        androidService.stopAdbKit(mobileId);
        return Response.success("停止完成");
    }

    @PostMapping("/aaptDumpBadging")
    public Response aaptDumpBadging(@RequestBody String apkDownloadUrl) {
        String res = androidService.aaptDumpBadging(apkDownloadUrl);
        return Response.success("ok", res);
    }

    @GetMapping("{mobileId}/imeList")
    public Response getImeList(@PathVariable String mobileId) {
        return Response.success(androidService.getImeList(mobileId));
    }

    @PostMapping("{mobileId}/ime")
    public Response setIme(@PathVariable String mobileId, String ime) {
        androidService.setIme(mobileId, ime);
        return Response.success("设置输入法成功");
    }
}