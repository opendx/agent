package com.fgnb.controller;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.uiautomator.UiautomatorServerManager;
import com.fgnb.model.Device;
import com.fgnb.vo.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import static io.restassured.RestAssured.get;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/device")
@Slf4j
public class DeviceController {
    /**
     * 根据设备ID 启动UIAutomator服务
     * @return 返回启动UIauotmator服务详细信息
     */
    @GetMapping("/openUiAutomatorServer")
    public Response openUiAutomatorServer(String deviceId){
        if(StringUtils.isEmpty(deviceId)){
            return Response.fail("设备id不能为空");
        }
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceId(deviceId);

        AndroidDevice androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        if(androidDevice == null){
            deviceInfo.setMsg("设备未接入");
            deviceInfo.setCanUse(false);
            return Response.fail(deviceInfo);
        }
        if(!androidDevice.isConnected()){
            deviceInfo.setMsg("设备未连接");
            deviceInfo.setCanUse(false);
            return Response.fail(deviceInfo);
        }
        if(androidDevice.getDevice().getStatus() != Device.IDLE_STATUS){
            deviceInfo.setMsg("设备未闲置");
            deviceInfo.setCanUse(false);
            return Response.fail(deviceInfo);
        }

        try {
            int port = openUiAutomatorServer(androidDevice,15);
            deviceInfo.setCanUse(true);
            deviceInfo.setPort(port);
            deviceInfo.setMsg("启动UiAutomatorServer成功");
            //将设备改为使用中 防止其他人远程操控
            androidDevice.getDevice().setStatus(Device.USING_STATUS);
        } catch (Exception e) {
            log.error("[{}]启动uiautomator服务失败",deviceId,e);
            deviceInfo.setMsg(e.getMessage());
            deviceInfo.setCanUse(false);
            return Response.fail(deviceInfo);
        }
        return Response.success(deviceInfo);
    }

    /**
     * 启动UiAutomatorServer
     * @param androidDevice
     * @param maxWaitSeconds 最大等待时间
     * @return uiautomator本地agent端口
     * @throws Exception
     */
    private int openUiAutomatorServer(AndroidDevice androidDevice,long maxWaitSeconds) throws Exception{
        UiautomatorServerManager uiautomatorServerManager = new UiautomatorServerManager(androidDevice);
        //以后重构 先这样处理   AndroidDevice code line 70 用于停掉自动化测试开启的uiautomatorserver
        androidDevice.setUiautomatorServerManager(uiautomatorServerManager);
        uiautomatorServerManager.startServer();
        uiautomatorServerManager.createForward();
        int port = uiautomatorServerManager.getPort();
        long startTime = System.currentTimeMillis();
        while(true){
            if(System.currentTimeMillis() - startTime > maxWaitSeconds*1000){
                throw new RuntimeException("超时未检测到uiautomator server运行");
            }
            try{
                String pingResp = get("http://127.0.0.1:" + port + "/wd/hub/session/888/ping").asString();
                if("pong".equals(pingResp)){
                    return port;
                }
            }catch (Exception e){
                //ignore
            }
        }
    }
    @Data
    public static class DeviceInfo {
        private Boolean canUse;
        private String deviceId;
        private Integer port;
        private String msg;
    }
}
