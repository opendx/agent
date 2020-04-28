package com.daxiang.server;

import com.daxiang.App;
import com.daxiang.model.Device;
import com.daxiang.model.Response;
import com.daxiang.model.UploadFile;
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
public class ServerClient {

    private static ServerClient INSTANCE;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${server}/upload/file/{fileType}")
    private String uploadFileUrl;

    @Value("${server}/device/list")
    private String deviceListUrl;
    @Value("${server}/device/save")
    private String deviceSaveUrl;

    @Value("${server}/driver/downloadUrl")
    private String driverDownloadUrlUrl;

    @Value("${server}/deviceTestTask/update")
    private String updateDeviceTestTaskUrl;
    @Value("${server}/deviceTestTask/firstUnStart/device/{deviceId}")
    private String findFirstUnStartDeviceTestTaskUrl;
    @Value("${server}/deviceTestTask/{deviceTestTaskId}/updateTestcase")
    private String updateTestcaseUrl;

    public static ServerClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = App.getBean(ServerClient.class);
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

        Response<List<Device>> response = restTemplate.exchange(deviceListUrl,
                HttpMethod.POST,
                new HttpEntity(params, headers),
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

        Response<Map<String, String>> response = restTemplate.exchange(driverDownloadUrlUrl,
                HttpMethod.POST,
                new HttpEntity<>(params),
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
        Response response = restTemplate.postForObject(deviceSaveUrl, device, Response.class);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMsg());
        }
    }

    /**
     * 上传文件
     *
     * @return 下载地址
     */
    public UploadFile uploadFile(File file, Integer fileType) {
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("file", new FileSystemResource(file));

        Response<UploadFile> response = restTemplate.exchange(uploadFileUrl,
                HttpMethod.POST,
                new HttpEntity<>(multiValueMap),
                new ParameterizedTypeReference<Response<UploadFile>>() {
                },
                fileType).getBody();

        if (response.isSuccess()) {
            return response.getData();
        } else {
            throw new RuntimeException(response.getMsg());
        }
    }

    /**
     * 更新DeviceTestTask
     */
    public void updateDeviceTestTask(DeviceTestTask deviceTestTask) {
        Response response = restTemplate.postForObject(updateDeviceTestTaskUrl, deviceTestTask, Response.class);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMsg());
        }
    }

    /**
     * 获取最早的未开始的测试任务
     */
    public DeviceTestTask getFirstUnStartDeviceTestTask(String deviceId) {
        Response<DeviceTestTask> response = restTemplate.exchange(findFirstUnStartDeviceTestTaskUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Response<DeviceTestTask>>() {
                },
                deviceId).getBody();

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
        Response response = restTemplate.postForObject(updateTestcaseUrl, testcase, Response.class, deviceTestTaskId);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMsg());
        }
    }
}
