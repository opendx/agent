package com.daxiang.controller;

import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Response;
import com.google.common.collect.ImmutableMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jiangyitao.
 */
@RequestMapping("/appium")
@RestController
public class AppiumController {

    @GetMapping("/version")
    public Response getVersion() {
        return Response.success(ImmutableMap.of("verison", AppiumServer.getVersion()));
    }
}
