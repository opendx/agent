package com.fgnb.api;

import com.alibaba.fastjson.JSON;
import com.fgnb.bean.Device;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

import static io.restassured.RestAssured.given;

/**
 * Created by jiangyitao.
 */
@Component
@Slf4j
public class UIServerApi {

    @Value("${uiServerHost}/device/add")
    private String addNewDeviceApi;

    //查找手机
    @Value("${uiServerHost}/device/findById")
    private String findDeviceApi;
    //上传文件
    @Value("${uiServerHost}/upload/file")
    private String uploadFileApi;
    //设备在线
    @Value("${uiServerHost}/device/save")
    private String deviceSaveApi;

    /**
     * 检查设备是否初始化过
     * @return
     */
    public Device findById(String deviceId) throws Exception{
        log.info("[{}]检查设备是否初始化",deviceId);
        Response response = given().param("deviceId", deviceId).get(findDeviceApi);
        log.info("[{}]服务器返回:{}",deviceId,response.asString());
        Object data = response.path("data");
        if(data == null){
            return null;
        }else{
            return JSON.parseObject(JSON.toJSONString(data),Device.class);
        }
    }

    /**
     * 保存设备信息
     * @param device
     */
    public void save(Device device) throws Exception{
        log.info("[{}]保存设备:\n{}", device.getDeviceId(),device);
        Response response = given().contentType(ContentType.JSON).body(device).post(deviceSaveApi);
        log.info("[{}]服务器返回:{}",device.getDeviceId(),response.asString());
        if(!"1".equals(response.path("status"))){
            throw new RuntimeException("保存设备失败");
        }
    }

    /**
     * 上传文件
     * @param
     * @return
     */
    public String uploadFile(File file) throws Exception{
        log.info("开始上传文件");
        Response response = given().multiPart("file", file).post(uploadFileApi);
        log.info("服务器响应:{}",response.asString());
        return response.path("data.downloadURL");
    }

}
