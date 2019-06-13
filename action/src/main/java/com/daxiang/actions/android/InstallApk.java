package com.daxiang.actions.android;

import com.daxiang.actions.utils.AndroidUtil;
import com.daxiang.actions.utils.MacacaUtil;
import io.restassured.RestAssured;
import macaca.client.MacacaClient;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.UUID;

/**
 * Created by jiangyitao.
 */
public class InstallApk {

    private MacacaClient driver;

    public InstallApk(MacacaClient driver) {
        this.driver = driver;
    }

    /**
     * 安装apk
     */
    public void excute(Object apkDownloadUrl, Object packageName) throws Exception {
        String deviceId = MacacaUtil.getDeviceId(driver);
        String _apkDownloadUrl = (String) apkDownloadUrl;
        String _packageName = (String) packageName;

        File apk = new File(UUID.randomUUID().toString().replaceAll("-", "") + ".apk");
        try {
            // download
            downloadApk(apk, _apkDownloadUrl);
            // push apk to device
            String remoteApkPath = AndroidUtil.pushApkToDevice(deviceId, apk.getAbsolutePath().replaceAll("\\\\", "/"));
            if (AndroidUtil.checkApkInstalled(deviceId, _packageName)) {
                // uninstall
                AndroidUtil.uninstallApk(deviceId, _packageName);
            }
            // install
            AndroidUtil.installApk(deviceId, remoteApkPath);
        } finally {
            FileUtils.deleteQuietly(apk);
        }
    }

    private void downloadApk(File apk, String downloadUrl) throws Exception {
        try (InputStream inputStream = RestAssured.get(downloadUrl).getBody().asInputStream();
             FileOutputStream fos = new FileOutputStream(apk)) {
            int bytesRead;
            int bufferSize = 8192;
            byte[] buffer = new byte[bufferSize];
            while ((bytesRead = inputStream.read(buffer, 0, bufferSize)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
}
