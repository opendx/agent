package com.daxiang.core.mobile.android.stf;

import com.android.ddmlib.*;
import com.daxiang.core.mobile.android.AndroidUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class MinicapInstaller {

    private IDevice iDevice;

    public MinicapInstaller(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    /**
     * 安装minicap
     */
    public void install() throws StfComponentInstallException {
        try {
            String mobileId = iDevice.getSerialNumber();

            String cpuAbi = AndroidUtil.getCpuAbi(iDevice);
            int sdkVersion = AndroidUtil.getSdkVersion(iDevice);

            String localMinicapPath = String.format(Minicap.LOCAL_MINICAP_PATH, cpuAbi);
            String localMinicapSoPath = String.format(Minicap.LOCAL_MINICAP_SO_PATH, sdkVersion, cpuAbi);

            // push minicap to mobile
            log.info("[{}]push minicap to mobile, {} -> {}", mobileId, localMinicapPath, Minicap.REMOTE_MINICAP_PATH);
            iDevice.pushFile(localMinicapPath, Minicap.REMOTE_MINICAP_PATH);

            // push minicap.so to mobile
            log.info("[{}]push minicap.so to mobile, {} -> {}", mobileId, localMinicapSoPath, Minicap.REMOTE_MINICAP_SO_PATH);
            iDevice.pushFile(localMinicapSoPath, Minicap.REMOTE_MINICAP_SO_PATH);

            String chmodCmd = String.format("chmod 777 %s %s", Minicap.REMOTE_MINICAP_PATH, Minicap.REMOTE_MINICAP_SO_PATH);
            log.info("[{}]{} ", mobileId, chmodCmd);
            iDevice.executeShellCommand(chmodCmd, new NullOutputReceiver());
        } catch (Exception e) {
            throw new StfComponentInstallException(e);
        }
    }
}
