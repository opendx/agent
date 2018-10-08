package com.fgnb.controller;

import com.alibaba.fastjson.JSONObject;
import com.fgnb.service.TaskService;
import com.fgnb.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/task")
@Slf4j
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * ui-server提交过来的测试任务
     * @return
     */
    @PostMapping("/commit")
    public Response commit(@RequestBody JSONObject jsonObject){
        try {
            taskService.commit(jsonObject.getString("deviceId"), (Map<String, String>) jsonObject.get("codes"));
            return Response.success("任务提交成功");
        } catch (Exception e) {
            log.error("任务提交失败",e);
            return Response.fail(e.getMessage());
        }
    }
}
