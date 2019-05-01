package com.fgnb.controller;

import com.fgnb.excutor.Excutor;
import com.fgnb.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


import java.util.Map;


/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/debug")
@Slf4j
public class DebugController {

    /**
     * 调试action
     * @return
     */
    @PostMapping("/debugAction")
    public Response debugAction(@RequestBody Map<String,String> testNGCodeMap){
        try {
            Excutor excutor = new Excutor();
            //编译
            Class clazz = excutor.compiler(testNGCodeMap.get("testClassName"), testNGCodeMap.get("testNGCode"));
            //testng运行
            excutor.debugActionByTestNG(new Class[]{clazz});
        }catch (Exception e){
            log.error("调试action出错",e);
            return Response.fail(e.getMessage());
        }
        return Response.success("执行成功");
    }
}
