package com.daxiang.api;

import com.daxiang.App;
import com.daxiang.model.Device;
import com.daxiang.model.Response;
import com.daxiang.model.devicetesttask.DeviceTestTask;
import com.daxiang.model.devicetesttask.Testcase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by jiangyitao.
 */
@Component
@Slf4j
public class MasterApi {

    private static MasterApi INSTANCE;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${master}/upload/file")
    private String uploadFileApi;

    @Value("${master}/device/list")
    private String deviceListApi;
    @Value("${master}/device/save")
    private String deviceSaveApi;

    @Value("${master}/driver/downloadUrl")
    private String driverDownloadUrlApi;

    @Value("${master}/deviceTestTask/update")
    private String updateDeviceTestTaskApi;
    @Value("${master}/deviceTestTask/firstUnStart/device/%s")
    private String findFirstUnStartDeviceTestTaskApi;
    @Value("${master}/deviceTestTask/%d/updateTestcase")
    private String updateTestcaseApi;

    public static MasterApi getInstance() {
        if (INSTANCE == null) {
            INSTANCE = App.getBean(MasterApi.class);
        }
        return INSTANCE;
    }

    /**
     * 通过设备id获取Device
     *
     * @return
     */
    public Device getDeviceById(String deviceId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", deviceId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(params, headers);

        Response<List<Device>> response = restTemplate.exchange(deviceListApi, HttpMethod.POST, requestEntity,
                new ParameterizedTypeReference<Response<List<Device>>>() {
                }).getBody();

        if (response.isSuccess()) {
            return response.getData().stream().findFirst().orElse(null);
        } else {
            throw new RuntimeException(response.getMsg());
        }
    }

    /**
     * 获取chromedriver下载地址
     *
     * @param deviceId
     * @return
     */
    public Optional<String> getChromedriverDownloadUrl(String deviceId) {
        Assert.hasText(deviceId, "deviceId不能为空");

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("deviceId", deviceId);
        params.add("type", 1); // chromedriver
        params.add("platform", OS.isFamilyWindows() ? 1 : OS.isFamilyMac() ? 3 : 2); // 1.windows 2.linux 3.macos

        Response<Map<String, String>> response = restTemplate.exchange(driverDownloadUrlApi, HttpMethod.POST, new HttpEntity<>(params),
                new ParameterizedTypeReference<Response<Map<String, String>>>() {
                }).getBody();

        if (response.isSuccess()) {
            if (response.getData() == null) {
                return Optional.empty();
            } else {
                return Optional.of(response.getData().get("downloadUrl"));
            }
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
     * @return 下载地址
     */
    public String uploadFile(File file, Integer fileType) {
        FileSystemResource resource = new FileSystemResource(file);
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("file", resource);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(multiValueMap);

        Response<Map<String, String>> response = restTemplate.exchange(uploadFileApi + "?fileType=" + fileType, HttpMethod.POST, httpEntity,
                new ParameterizedTypeReference<Response<Map<String, String>>>() {
                }).getBody();

        if (response.isSuccess()) {
            return response.getData().get("downloadURL");
        } else {
            throw new RuntimeException(response.getMsg());
        }
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
     * 获取最早的未开始的测试任务
     */
    public DeviceTestTask getFirstUnStartDeviceTestTask(String deviceId) {
        String url = String.format(findFirstUnStartDeviceTestTaskApi, deviceId);
        Response<DeviceTestTask> response = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Response<DeviceTestTask>>() {
                }).getBody();

        if (response.isSuccess()) {
            return response.getData();
        } else {
            throw new RuntimeException(response.getMsg());
        }
    }

    /**
     * 更新测试用例执行状态
     *
     * @param deviceTestTaskId
     * @param testcase
     */
    public void updateTestcase(Integer deviceTestTaskId, Testcase testcase) {
        String url = String.format(updateTestcaseApi, deviceTestTaskId);
        Response response = restTemplate.postForObject(url, testcase, Response.class);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMsg());
        }
    }
}
