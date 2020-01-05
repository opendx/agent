package com.daxiang.core.android.stf;

import com.android.ddmlib.*;
import com.daxiang.core.android.AndroidUtil;
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
            String deviceId = iDevice.getSerialNumber();

            String cpuAbi = AndroidUtil.getCpuAbi(iDevice);
            String sdkVersion = AndroidUtil.getSdkVersion(iDevice);

            String localMinicapPath = String.format(Minicap.LOCAL_MINICAP_PATH, cpuAbi);
            String localMinicapSoPath = String.format(Minicap.LOCAL_MINICAP_SO_PATH, sdkVersion, cpuAbi);

            // push minicap to device
            log.info("[minicap][{}]push minicap to device, {} -> {}", deviceId, localMinicapPath, Minicap.REMOTE_MINICAP_PATH);
            iDevice.pushFile(localMinicapPath, Minicap.REMOTE_MINICAP_PATH);

            // push minicap.so to device
            log.info("[minicap][{}]push minicap.so to device, {} -> {}", deviceId, localMinicapSoPath, Minicap.REMOTE_MINICAP_SO_PATH);
            iDevice.pushFile(localMinicapSoPath, Minicap.REMOTE_MINICAP_SO_PATH);

            String chmodCmd = String.format("chmod 777 %s %s", Minicap.REMOTE_MINICAP_PATH, Minicap.REMOTE_MINICAP_SO_PATH);
            log.info("[minicap][{}]{} ", deviceId, chmodCmd);
            iDevice.executeShellCommand(chmodCmd, new NullOutputReceiver());
        } catch (Exception e) {
            throw new StfComponentInstallException(e);
        }
    }
}
