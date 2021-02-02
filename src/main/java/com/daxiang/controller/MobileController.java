package com.daxiang.controller;

import com.daxiang.core.mobile.Mobile;
import com.daxiang.model.Response;
import com.daxiang.service.MobileService;
import com.google.common.collect.ImmutableMap;
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
        mobileService.installApp(app, mobileId);
        return Response.success("安装成功");
    }

    @GetMapping("/{mobileId}")
    public Response getMobile(@PathVariable String mobileId) {
        return Response.success(mobileService.getMobile(mobileId));
    }

    @DeleteMapping("/{mobileId}")
    public Response delete(@PathVariable String mobileId) {
        Mobile mobile = mobileService.delete(mobileId);
        return Response.success(mobile);
    }

    @GetMapping("/{mobileId}/session/{sessionId}/startLogsBroadcast")
    public Response startLogsBroadcast(@PathVariable String sessionId, @PathVariable String mobileId) {
        return Response.success(ImmutableMap.of("wsUrl", mobileService.startLogsBroadcast(mobileId, sessionId)));
    }

    @GetMapping("/{mobileId}/stopLogsBroadcast")
    public Response stopLogsBroadcast(@PathVariable String mobileId) {
        mobileService.stopLogsBroadcast(mobileId);
        return Response.success();
    }
}
