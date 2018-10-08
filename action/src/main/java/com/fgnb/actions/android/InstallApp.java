package com.fgnb.actions.android;

import com.fgnb.actions.Action;
import com.fgnb.actions.utils.AndroidUtil;
import com.fgnb.actions.utils.MacacaUtil;
import io.restassured.RestAssured;
import macaca.client.MacacaClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jiangyitao.
 */
public class InstallApp extends Action {

    public InstallApp(MacacaClient macacaClient) {
        super(macacaClient);
    }

    @Override
    public String excute(String... params) throws Exception {
        String appDownloadURL = params[0];
        String packageName = params[1];
        String deviceId = MacacaUtil.getDeviceId(macacaClient);

        File appFile = new File(UUID.randomUUID().toString().replaceAll("-","")+".apk");
        //1.下载APP
        try {
            System.out.println("下载APP -> "+appDownloadURL);
            downloadApp(appFile,appDownloadURL);
            System.out.println("下载完成 <- "+appDownloadURL);
        }catch (Exception e){
            throw new RuntimeException(appDownloadURL + "下载APP失败");
        }
        //2.push到手机
        String phoneAppPath = AndroidUtil.pushAppToDeviceByAdbShell(deviceId, appFile.getAbsolutePath().replaceAll("\\\\","/"));

        if(AndroidUtil.isInstalledApp(deviceId,packageName)){
            //3.卸载App
            AndroidUtil.uninstallAppByAdbShell(deviceId,packageName);
        }

        //5.安装APP
        AndroidUtil.installAppByAdbShell(deviceId,phoneAppPath);

        //6.删除APP
        if(appFile!=null){
            appFile.delete();
            System.out.println("删除下载的APP文件成功");
        }
        return null;
    }

    private void downloadApp(File file,String downloadURL) throws Exception{
        InputStream appInputStream = null;
        FileOutputStream fos = null;
        try {
            appInputStream = RestAssured.get(downloadURL).getBody().asInputStream();
            fos = new FileOutputStream(file);
            int bytesRead;
            int bufferSize = 8192;
            byte[] buffer = new byte[bufferSize];
            while ((bytesRead = appInputStream.read(buffer, 0, bufferSize)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }finally {
            if(fos!=null){
                fos.close();
            }
            if(appInputStream!=null){
                appInputStream.close();
            }
        }
    }

}
