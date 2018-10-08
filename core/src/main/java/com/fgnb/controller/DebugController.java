package com.fgnb.controller;

import com.fgnb.excutor.Excutor;
import com.fgnb.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

import static io.restassured.RestAssured.get;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/debug")
@Slf4j
public class DebugController {
    /**
     * 检查设备是否可以调试action
     * @return
     */
    @GetMapping("/checkDeviceCanDebugAction")
    public Response checkDeviceCanDebugAction(Integer uiautomator2Port){
        try {
            String pingResp = get("http://127.0.0.1:" + uiautomator2Port + "/wd/hub/session/888/ping").asString();
            if("pong".equals(pingResp)){
                return Response.success("当前端口可调试action");
            }
        }catch (Exception e){
            log.error("检查设备是否可以调试action出错",e);
        }
        return Response.fail("无法调试action，请先选择一台设备进行远程控制");
    }

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
