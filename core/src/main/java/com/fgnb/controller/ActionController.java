package com.fgnb.controller;

import com.fgnb.model.Response;
import com.fgnb.model.request.ActionDebugRequest;
import com.fgnb.service.ActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/action")
@Slf4j
public class ActionController {

    @Autowired
    private ActionService actionService;

    /**
     * 调试action
     */
    @PostMapping("/debug")
    public Response debug(@Valid @RequestBody ActionDebugRequest request){
        return actionService.debug(request);
    }
}
