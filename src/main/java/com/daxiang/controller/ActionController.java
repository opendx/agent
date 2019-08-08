package com.daxiang.controller;

import com.daxiang.model.Response;
import com.daxiang.model.request.ActionDebugRequest;
import com.daxiang.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/action")
public class ActionController {

    @Autowired
    private ActionService actionService;

    /**
     * 调试action
     */
    @PostMapping("/debug")
    public Response debug(@Valid @RequestBody ActionDebugRequest request) {
        return actionService.debug(request);
    }

    /**
     * 给开发者调试专用
     *
     * @param code
     * @return
     */
    @PostMapping("/developer/debug")
    public Response developerDebug(String className, String code) {
        return actionService.developerDebug(className, code);
    }
}
