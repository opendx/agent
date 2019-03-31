package com.fgnb.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fgnb.bean.Device;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;

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
    @Value("${uiServerHost}/device/list")
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
        Response response = given().param("id", deviceId).post(findDeviceApi);
        log.info("[{}]服务器返回:{}",deviceId,response.asString());
        List<Map> data = response.path("data");
        if(CollectionUtils.isEmpty(data)){
            return null;
        }else{
            return JSON.parseObject(JSON.toJSONString(data.get(0)),Device.class);
        }
    }

    /**
     * 保存设备信息
     * @param device
     */
    public void save(Device device) throws Exception{
        log.info("[{}]保存设备:\n{}", device.getId(),device);
        Response response = given().contentType(ContentType.JSON).body(device).post(deviceSaveApi);
        log.info("[{}]服务器返回:{}",device.getId(),response.asString());
        if((int)response.path("status") != 1){
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
