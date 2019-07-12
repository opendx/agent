package com.daxiang.android.stf;

import com.android.ddmlib.*;
import com.daxiang.android.AndroidDevice;
import com.daxiang.android.AndroidUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class MinicapInstaller {

    private static final String MINICAP_CHMOD_SHELL = "chmod 777 %s %s";

    private static final String MINICAP_PATH = "vendor/minicap/bin/%s/minicap";
    private static final String MINICAP_SO_PATH = "vendor/minicap/shared/android-%s/%s/minicap.so";

    private IDevice iDevice;

    public MinicapInstaller(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    /**
     * 安装minicap
     */
    public void install() throws TimeoutException, AdbCommandRejectedException, SyncException, IOException, ShellCommandUnresponsiveException {
        String deviceId = iDevice.getSerialNumber();

        String cpuAbi = AndroidUtil.getCpuAbi(iDevice);
        String apiLevel = AndroidUtil.getApiLevel(iDevice);

        String minicapFilePath = String.format(MINICAP_PATH, cpuAbi);
        String minicapSoFilePath = String.format(MINICAP_SO_PATH, apiLevel, cpuAbi);

        // push minicap 到手机
        String phoneMinicapPath = AndroidDevice.TMP_FOLDER + "minicap";
        log.info("[{}][minicap]push minicap到手机,{} -> {}", deviceId, minicapFilePath, phoneMinicapPath);
        iDevice.pushFile(minicapFilePath, phoneMinicapPath);

        // push minicap.so 到手机
        String phoneMinicapSoPath = AndroidDevice.TMP_FOLDER + "minicap.so";
        log.info("[{}][minicap]push minicap.so到手机,{} -> {}", deviceId, minicapSoFilePath, phoneMinicapSoPath);
        iDevice.pushFile(minicapSoFilePath, phoneMinicapSoPath);

        // 给手机里的minicap/minicap.so 赋予777权限
        String chmodShellCmd = String.format(MINICAP_CHMOD_SHELL, phoneMinicapPath, phoneMinicapSoPath);
        log.info("[{}][minicap]{} ", deviceId, chmodShellCmd);
        iDevice.executeShellCommand(chmodShellCmd, new NullOutputReceiver());
    }
}
