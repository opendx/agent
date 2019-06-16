package com.daxiang.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daxiang.actions.utils.AndroidUtil;
import com.daxiang.android.AndroidDevice;
import com.daxiang.android.AndroidDeviceHolder;
import com.daxiang.android.AndroidUtils;
import com.daxiang.api.MasterApi;
import com.daxiang.exception.BusinessException;
import com.daxiang.model.Response;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


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

    public Response startUiautomator2server(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);
        try {
            int port = androidDevice.getUiautomator2Server().start();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("port", port);
            return Response.success(jsonObject);
        } catch (Exception e) {
            log.error("[{}]启动uiautomator2server出错", deviceId, e);
            return Response.fail(e.getMessage());
        }
    }

    public Response dump(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);

        int localPort = androidDevice.getUiautomator2Server().getLocalPort();
        if (localPort <= 0) {
            return Response.fail("未开启Uiautomator2Server");
        }

        String url = "http://127.0.0.1:" + localPort + "/wd/hub/session/888/source";
        String uiautomator2ServerResponse;
        try {
            uiautomator2ServerResponse = restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("[{}]请求{}失败", deviceId, url, e);
            return Response.fail("请求Uiautomator2Server失败，请确认是否已打开Uiautomator2Server");
        }

        if (StringUtils.isEmpty(uiautomator2ServerResponse)) {
            return Response.fail("Uiautomator2Server未返回dump数据");
        }

        JSONObject uiautomator2ServerResponseObject = JSON.parseObject(uiautomator2ServerResponse);
        if (uiautomator2ServerResponseObject.getInteger("status") != 0) {
            return Response.fail("Uiautomator2Server返回状态错误");
        }

        String dumpXml = uiautomator2ServerResponseObject.getString("value");
        if (StringUtils.isEmpty(dumpXml)) {
            return Response.fail("Uiautomator2Server dump数据为空");
        }

        return Response.success("ok", XML.toJSONObject(dumpXml).toString());
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
            AndroidUtils.installApk(androidDevice.getIDevice(), apkPath);
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
        String screenshotFilePath = UUIDUtil.getUUID() + ".jpg";
        File screenshotFile = null;
        try {
            AndroidUtils.screenshotByMinicap(androidDevice.getIDevice(), screenshotFilePath, androidDevice.getResolution());
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
