package com.daxiang.controller;

import com.daxiang.model.Response;
import com.daxiang.service.AgentExtJarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/agentExtJar")
public class AgentExtJarController {

    @Autowired
    private AgentExtJarService agentExtJarService;

    @PostMapping("/load")
    public Response loadExtJar(@RequestBody String jarUrl) {
        agentExtJarService.loadJar(jarUrl);
        return Response.success("加载成功");
    }
}
