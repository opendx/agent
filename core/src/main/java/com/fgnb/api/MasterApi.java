package com.fgnb.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fgnb.App;
import com.fgnb.model.Device;
import com.fgnb.model.Response;
import com.fgnb.model.devicetesttask.DeviceTestTask;
import com.fgnb.model.devicetesttask.Testcase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Component
@Slf4j
public class MasterApi {

    private static MasterApi masterApi;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${master}/upload/file")
    private String uploadFileApi;

    @Value("${master}/device/list")
    private String deviceListApi;
    @Value("${master}/device/save")
    private String deviceSaveApi;

    @Value("${master}/deviceTestTask/update")
    private String updateDeviceTestTaskApi;
    @Value("${master}/deviceTestTask/unStart")
    private String findUnStartDeviceTestTasksByDeviceIdsApi;
    @Value("${master}/deviceTestTask/updateTestcase/")
    private String updateTestcaseApi;

    public synchronized static MasterApi getInstance() {
        if(masterApi == null) {
            masterApi = App.getBean(MasterApi.class);
        }
        return masterApi;
    }

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
        } else {
            throw new RuntimeException(response.getMsg());
        }
    }

    /**
     * 保存设备
     *
     * @param device
     */
    public void saveDevice(Device device) {
        Response response = restTemplate.postForObject(deviceSaveApi, device, Response.class);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMsg());
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
            throw new RuntimeException(response.getMsg());
        }

        Map<String, String> data = JSON.parseObject(JSON.toJSONString(response.getData()), Map.class);
        return data.get("downloadURL");
    }

    /**
     * 更新DeviceTestTask
     */
    public void updateDeviceTestTask(DeviceTestTask deviceTestTask) {
        Response response = restTemplate.postForObject(updateDeviceTestTaskApi, deviceTestTask, Response.class);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMsg());
        }
    }

    /**
     * 获取未开始的测试任务
     */
    public List<DeviceTestTask> findUnStartDeviceTestTasksByDeviceIds(List<String> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return new ArrayList<>();
        }

        String param = "?deviceIds=" + deviceIds.stream().collect(Collectors.joining(","));
        Response response = restTemplate.getForObject(findUnStartDeviceTestTasksByDeviceIdsApi + param, Response.class);
        if (response.isSuccess()) {
            return JSON.parseArray(JSONArray.toJSONString(response.getData()), DeviceTestTask.class);
        } else {
            throw new RuntimeException(response.getMsg());
        }
    }

    /**
     * 更新测试用例执行状态
     * @param deviceTestTaskId
     * @param testcase
     */
    public void updateTestcase(Integer deviceTestTaskId, Testcase testcase) {
        String url = updateTestcaseApi + deviceTestTaskId;
        Response response = restTemplate.postForObject(url, testcase, Response.class);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMsg());
        }
    }
}
