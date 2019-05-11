package com.fgnb.actions.android;

import com.fgnb.actions.utils.AndroidUtil;
import com.fgnb.actions.utils.MacacaUtil;
import io.restassured.RestAssured;
import macaca.client.MacacaClient;

import java.io.*;
import java.util.UUID;

/**
 * Created by jiangyitao.
 */
public class InstallApp {

    private MacacaClient macacaClient;

    public InstallApp(MacacaClient macacaClient) {
        this.macacaClient = macacaClient;
    }

    /**
     * 安装APK
     *
     * @param appDownloadURL
     * @param packageName
     * @return
     * @throws IOException
     */
    public void excute(String appDownloadURL, String packageName) throws Exception {
        String deviceId = MacacaUtil.getDeviceId(macacaClient);

        File appFile = new File(UUID.randomUUID().toString().replaceAll("-", "") + ".apk");
        try {
            //1.下载APP
            System.out.println("下载APP -> " + appDownloadURL);
            downloadApp(appFile, appDownloadURL);
            System.out.println("下载完成 <- " + appDownloadURL);

            //2.push到手机
            String phoneAppPath = AndroidUtil.pushAppToDeviceByAdbShell(deviceId, appFile.getAbsolutePath().replaceAll("\\\\", "/"));

            if (AndroidUtil.isInstalledApp(deviceId, packageName)) {
                //3.卸载App
                AndroidUtil.uninstallAppByAdbShell(deviceId, packageName);
            }

            //5.安装APP
            AndroidUtil.installAppByAdbShell(deviceId, phoneAppPath);
        } finally {
            appFile.delete();
            System.out.println("删除下载的APP文件成功");
        }

    }

    private void downloadApp(File file, String downloadURL) throws Exception {
        try (InputStream appInputStream = RestAssured.get(downloadURL).getBody().asInputStream();
             FileOutputStream fos = new FileOutputStream(file)){
            int bytesRead;
            int bufferSize = 8192;
            byte[] buffer = new byte[bufferSize];
            while ((bytesRead = appInputStream.read(buffer, 0, bufferSize)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
}
