package com.fgnb.controller;

import com.fgnb.service.AppInspectorService;
import com.fgnb.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jiangyitao.
 */
@RequestMapping("/inspector")
@RestController
@Slf4j
public class AppInspectorController {

    @Autowired
    private AppInspectorService appInspectorService;

    @RequestMapping("/android/dump")
    public Object getAndroidDumpJson(String deviceId){
        try {
            return appInspectorService.getAndroidDumpJson(deviceId);
        } catch (Exception e) {
            log.error("dump出错",e);
            return e.getMessage();
        }
    }

    @RequestMapping("/android/screenshot")
    public Response getScreenShot(String deviceId){
        try {
            return Response.success("获取截图成功",appInspectorService.getScreenShot(deviceId));
        } catch (Exception e) {
            log.error("截图出错",e);
            return Response.error("截图失败",e.getMessage());
        }
    }

}
