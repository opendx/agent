package com.fgnb.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fgnb.model.Device;
import com.fgnb.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by jiangyitao.
 */
@Component
@Slf4j
public class MasterApi {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${master}/device/list")
    private String deviceListApi;

    @Value("${master}/device/save")
    private String deviceSaveApi;

    @Value("${master}/upload/file")
    private String uploadFileApi;

    /**
     * 通过设备id获取Device
     *
     * @return
     */
    public Device getDeviceById(String deviceId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", deviceId);

        Response response = restTemplate.postForObject(deviceListApi, params, Response.class);
        if (response.isSuccess()) {
            List<Device> devices = JSON.parseArray(JSONArray.toJSONString(response.getData()), Device.class);
            return devices.stream().findFirst().orElse(null);
        }
        return null;
    }

    /**
     * 保存设备
     *
     * @param device
     */
    public void saveDevice(Device device) {
        Response response = restTemplate.postForObject(deviceSaveApi, device, Response.class);
        if (!response.isSuccess()) {
            throw new RuntimeException("保存" + device.getId() + "失败");
        }
    }

    /**
     * 上传文件
     *
     * @param
     * @return 下载地址
     */
    public String uploadFile(File file) {
        FileSystemResource resource = new FileSystemResource(file);
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("file", resource);

        Response response = restTemplate.postForObject(uploadFileApi, multiValueMap, Response.class);
        if (!response.isSuccess()) {
            throw new RuntimeException("上传" + file.getName() + "失败");
        }

        Map<String, String> data = JSON.parseObject(JSON.toJSONString(response.getData()), Map.class);
        return data.get("downloadURL");
    }

}
