package com.daxiang.service;

import com.alibaba.fastjson.JSONObject;
import com.android.ddmlib.IDevice;
import com.daxiang.android.AndroidDevice;
import com.daxiang.android.AndroidDeviceHolder;
import com.daxiang.android.AndroidUtil;
import com.daxiang.api.MasterApi;
import com.daxiang.exception.BusinessException;
import com.daxiang.model.Response;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class AndroidService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MasterApi masterApi;

    public Response startAdbKit(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);
        try {
            int port = androidDevice.getAdbKit().start();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("port", port);
            return Response.success(jsonObject);
        } catch (IOException e) {
            log.error("启动adbkit失败", e);
            return Response.fail(e.getMessage());
        }
    }

    public Response stopAdbKit(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);
        androidDevice.getAdbKit().stop();
        return Response.success("停止完成");
    }

    public Response freshAndroidDriver(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);
        AppiumDriver appiumDriver = androidDevice.freshAndroidDriver();
        JSONObject data = new JSONObject();
        data.put("appiumSessionId", appiumDriver.getSessionId().toString());
        return Response.success(data);
    }

    public Response dump(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);

        AppiumDriver appiumDriver = androidDevice.getAppiumDriver();
        if (appiumDriver == null) {
            return Response.fail("androidDriver为空");
        }

        String pageSource = appiumDriver.getPageSource();
        if (StringUtils.isEmpty(pageSource)) {
            return Response.fail("pageSource为空");
        }

        // 由于appium pageSource返回的xml不是规范的xml，需要把除了hierarchy节点以外的节点替换成node，否则xml转json会出问题
        try (InputStream in = new ByteArrayInputStream(pageSource.getBytes())) {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(in);
            Element rootElement = document.getRootElement();
            handleElement(rootElement);
            return Response.success("ok", XML.toJSONObject(document.asXML()).toString());
        } catch (DocumentException e) {
            log.error("读取pageSource出错，pageSource: {}", pageSource, e);
            return Response.fail("读取pageSource出错，请稍后重试");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    private void handleElement(Element element) {
        if (element == null) {
            return;
        }

        String elementName = element.getName();
        if (StringUtils.isEmpty(elementName)) {
            return;
        }
        if (!"hierarchy".equals(elementName)) {
            element.setName("node");
        }

        List<Element> elements = element.elements();
        elements.forEach(e -> handleElement(e));
    }

    public Response screenshot(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);

        String downloadURL;
        try {
            downloadURL = screenshotByMinicapAndUploadToMaster(androidDevice);
        } catch (Exception e) {
            log.error("[{}]截图并上传到master失败", deviceId, e);
            return Response.fail(e.getMessage());
        }

        JSONObject response = new JSONObject();
        response.put("downloadURL", downloadURL);
        response.put("imgHeight", androidDevice.getDevice().getScreenHeight());
        response.put("imgWidth", androidDevice.getDevice().getScreenWidth());

        return Response.success(response);
    }

    public Response installApk(MultipartFile apk, String deviceId) {
        if (apk == null) {
            return Response.fail("apk不能为空");
        }
        if (!apk.getOriginalFilename().endsWith(".apk")) {
            return Response.fail("无法安装非APK文件");
        }

        AndroidDevice androidDevice = getAndroidDevice(deviceId);
        String apkPath = UUIDUtil.getUUID() + ".apk";
        File apkFile = new File(apkPath);
        try {
            FileUtils.copyInputStreamToFile(apk.getInputStream(), apkFile);
            AndroidUtil.installApk(androidDevice.getIDevice(), apkPath);
            return Response.success("安装成功");
        } catch (Exception e) {
            log.error("安装apk失败", e);
            return Response.fail(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(apkFile);
        }
    }

    private AndroidDevice getAndroidDevice(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            throw new BusinessException("设备id不能为空");
        }
        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if (androidDevice == null) {
            throw new BusinessException("设备不存在");
        }
        if (!androidDevice.isConnected()) {
            throw new BusinessException("设备未连接");
        }
        return androidDevice;
    }

    public String screenshotByMinicapAndUploadToMaster(AndroidDevice androidDevice) throws Exception {
        return screenshotByMinicapAndUploadToMaster(androidDevice.getIDevice(), androidDevice.getResolution());
    }

    public String screenshotByMinicapAndUploadToMaster(IDevice iDevice, String resolution) throws Exception {
        String screenshotFilePath = UUIDUtil.getUUID() + ".jpg";
        File screenshotFile = null;
        try {
            AndroidUtil.screenshotByMinicap(iDevice, screenshotFilePath, resolution);
            screenshotFile = new File(screenshotFilePath);
            return masterApi.uploadFile(screenshotFile);
        } finally {
            FileUtils.deleteQuietly(screenshotFile);
        }
    }

    public Response aaptDumpBadging(String apkDownloadUrl) {
        if (StringUtils.isEmpty(apkDownloadUrl)) {
            return Response.fail("apk下载地址不能为空");
        }

        byte[] apkByte = restTemplate.getForObject(apkDownloadUrl, byte[].class);
        File apk = new File(UUIDUtil.getUUID() + ".apk");
        try {
            FileUtils.writeByteArrayToFile(apk, apkByte, false);
            String result = AndroidUtil.aaptDumpBadging(apk.getAbsolutePath());
            return Response.success("ok", result);
        } catch (IOException e) {
            log.error("io error", e);
            return Response.fail(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(apk);
        }
    }
}
