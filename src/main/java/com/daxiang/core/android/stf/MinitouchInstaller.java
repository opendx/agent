package com.daxiang.core.android.stf;

import com.android.ddmlib.*;
import com.daxiang.core.android.AndroidUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class MinitouchInstaller {


    private IDevice iDevice;

    public MinitouchInstaller(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    /**
     * 安装minitouch
     */
    public void install() throws StfComponentInstallException {
        try {
            String deviceId = iDevice.getSerialNumber();

            String cpuAbi = AndroidUtil.getCpuAbi(iDevice);
            String localMinitouchPath = String.format(Minitouch.LOCAL_MINITOUCH_PATH, cpuAbi);

            log.info("[minitouch][{}]push minitouch to device, {} -> {}", deviceId, localMinitouchPath, Minitouch.REMOTE_MINITOUCH_PATH);
            iDevice.pushFile(localMinitouchPath, Minitouch.REMOTE_MINITOUCH_PATH);

            String chmodCmd = "chmod 777 " + Minitouch.REMOTE_MINITOUCH_PATH;
            log.info("[minitouch][{}]{} ", deviceId, chmodCmd);
            iDevice.executeShellCommand(chmodCmd, new NullOutputReceiver());
        } catch (Exception e) {
            throw new StfComponentInstallException(e);
        }
    }
}