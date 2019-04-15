package com.fgnb.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.stf.MinicapScreenShoter;
import com.fgnb.android.uiautomator.UiautomatorServerManager;
import com.fgnb.api.ServerApi;
import com.fgnb.utils.UUIDUtil;
import com.fgnb.model.InspctorImgVo;
import com.fgnb.websocket.UiAutomator2SocketServer;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;

import static io.restassured.RestAssured.get;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class AppInspectorService {


    @Autowired
    private ServerApi uiServerApi;

    public String getAndroidDumpJson(String deviceId) throws Exception{
        UiautomatorServerManager uiautomatorServerManager = UiAutomator2SocketServer.uiautomatorServerManagerMap.get(deviceId);
        if(uiautomatorServerManager==null){
            throw new RuntimeException("没有得到uiautomatorServerManager，无法dump布局");
        }
        int port = uiautomatorServerManager.getPort();
        try {
            String url = "http://127.0.0.1:" + port + "/wd/hub/session/888/source";
            log.info("send get request => {}",url);
            Response response = get(url);
            String resp = response.asString();
            log.info("uiautomator2server resp => {}", resp);
            JSONObject jsonObject = JSON.parseObject(resp);
            if(jsonObject.getInteger("status") == 0){
                String value = jsonObject.getString("value");
                if(!StringUtils.isEmpty(value)){
                    return XML.toJSONObject(value).toString();
                }
            }
        }catch (Exception e){
            log.error("获取布局失败",e);
        }
        throw new RuntimeException("获取布局失败，请稍后重试");
    }

    public InspctorImgVo getScreenShot(String deviceId) throws Exception{
        AndroidDevice androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        String localScreenshotPath = null;
        try {
            //本地 截图路径
            localScreenshotPath = UUIDUtil.getUUID()+".jpg";
            MinicapScreenShoter.takeScreenShot(localScreenshotPath,androidDevice);
            log.info("[{}]pull dump时的截图到{}成功",deviceId,localScreenshotPath);

            String downloadURL = uiServerApi.uploadFile(new File(localScreenshotPath));

            InspctorImgVo inspctorImgVo = new InspctorImgVo();
            inspctorImgVo.setDownloadURL(downloadURL);
            inspctorImgVo.setImgHeight(androidDevice.getDevice().getScreenHeight());
            inspctorImgVo.setImgWidth(androidDevice.getDevice().getScreenWidth());
            return inspctorImgVo;
        }finally {
            //删除生成的图片
            if(localScreenshotPath!=null){
                try {
                    new File(localScreenshotPath).delete();
                }catch (Exception e){
                    //ignore
                }
            }
        }
    }

}
