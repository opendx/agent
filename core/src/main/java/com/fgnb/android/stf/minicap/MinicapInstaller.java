package com.fgnb.android.stf.minicap;

import com.android.ddmlib.*;
import com.fgnb.android.AndroidUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Created by jiangyitao.
 * minicap安装器
 */
@Slf4j
public class MinicapInstaller {

    private static final String ANDROID_TMP_FOLDER = "/data/local/tmp/";
    private static final String MINICAP_CHMOD_SHELL = "chmod 777 %s %s";

    private static final String MINICAP_PATH = "vendor/minicap/bin/%s/minicap";
    private static final String MINICAP_SO_PATH = "vendor/minicap/shared/android-%s/%s/minicap.so";

    private IDevice iDevice;

    public MinicapInstaller(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    /**
     * 根据手机cpu架构/android版本 push相应的minicap文件到手机/data/local/tmp目录
     * 对minicap/minicap.so 文件赋予777权限
     */
    public void install() throws TimeoutException, AdbCommandRejectedException, SyncException, IOException, ShellCommandUnresponsiveException {
        String deviceId = iDevice.getSerialNumber();

        String cpuAbi = AndroidUtils.getCpuAbi(iDevice);
        String apiLevel = AndroidUtils.getApiLevel(iDevice);

        String minicapFilePath = String.format(MINICAP_PATH,cpuAbi);
        String minicapSoFilePath = String.format(MINICAP_SO_PATH,apiLevel,cpuAbi);

        //push minicap 到手机
        String phoneMinicapPath = ANDROID_TMP_FOLDER + "minicap";
        log.info("[{}]push minicap到手机,{} -> {}", deviceId, minicapFilePath, phoneMinicapPath);
        iDevice.pushFile(minicapFilePath, phoneMinicapPath);

        //push minicap.so 到手机
        String phoneMinicapSoPath = ANDROID_TMP_FOLDER + "minicap.so";
        log.info("[{}]push minicap.so到手机,{} -> {}", deviceId, minicapSoFilePath, phoneMinicapSoPath);
        iDevice.pushFile(minicapSoFilePath, phoneMinicapSoPath);

        //给手机里的minicap/minicap.so 赋予777权限
        String chmodShellCmd = String.format(MINICAP_CHMOD_SHELL, phoneMinicapPath, phoneMinicapSoPath);
        log.info("[{}]{} ", deviceId, chmodShellCmd);
        iDevice.executeShellCommand(chmodShellCmd, new NullOutputReceiver());
    }
}
