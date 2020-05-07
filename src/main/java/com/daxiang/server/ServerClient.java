package com.daxiang.server;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.core.pcweb.Browser;
import com.daxiang.model.Mobile;
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
import org.springframework.util.StringUtils;
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
    @Value("${server}/project/list")
    private String projectListUrl;
    @Value("${server}/mobile/list")
    private String deviceListUrl;
    @Value("${server}/mobile/save")
    private String deviceSaveUrl;
    @Value("${server}/browser/save")
    private String browserSaveUrl;
    @Value("${server}/driver/downloadUrl")
    private String driverDownloadUrl;

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

    public JSONObject getCapabilitiesByProjectId(Integer projectId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", projectId + "");

        Response<List<Map<String, String>>> response = restTemplate.exchange(projectListUrl,
                HttpMethod.POST,
                new HttpEntity(params, headers),
                new ParameterizedTypeReference<Response<List<Map<String, String>>>>() {
                }).getBody();

        if (response.isSuccess()) {
            Optional<Map<String, String>> project = response.getData().stream().findFirst();
            if (project.isPresent()) {
                String capabilities = project.get().get("capabilities");
                if (StringUtils.hasText(capabilities)) {
                    try {
                        return JSONObject.parseObject(capabilities);
                    } catch (Exception ign) {
                    }
                }
            }

            return new JSONObject();
        } else {
            throw new RuntimeException(response.getMsg());
        }
    }

    public Mobile getMobileById(String mobileId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", mobileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Response<List<Mobile>> response = restTemplate.exchange(deviceListUrl,
                HttpMethod.POST,
                new HttpEntity(params, headers),
                new ParameterizedTypeReference<Response<List<Mobile>>>() {
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
        // todo mobileId
        Assert.hasText(deviceId, "deviceId不能为空");

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("deviceId", deviceId);
        params.add("type", 1); // chromedriver
        params.add("platform", OS.isFamilyWindows() ? 1 : OS.isFamilyMac() ? 3 : 2); // 1.windows 2.linux 3.macos

        Response<Map<String, String>> response = restTemplate.exchange(driverDownloadUrl,
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

    public void saveDevice(Mobile mobile) {
        Response response = restTemplate.postForObject(deviceSaveUrl, mobile, Response.class);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMsg());
        }
    }

    public void saveBrowser(Browser browser) {
        Response response = restTemplate.postForObject(browserSaveUrl, browser, Response.class);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMsg());
        }
    }

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
