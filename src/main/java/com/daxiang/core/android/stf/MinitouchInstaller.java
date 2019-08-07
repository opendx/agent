package com.daxiang.core.android.stf;

import com.android.ddmlib.*;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.android.AndroidUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class MinitouchInstaller {

    private static final String MINITOUCH_PATH = "vendor/minitouch/%s/minitouch";
    private static final String MINITOUCH_CHMOD_SHELL = "chmod 777 %s";

    private IDevice iDevice;

    public MinitouchInstaller(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    /**
     * 安装minitouch
     */
    public void install() throws TimeoutException, AdbCommandRejectedException, SyncException, IOException, ShellCommandUnresponsiveException {
        String deviceId = iDevice.getSerialNumber();

        String cpuAbi = AndroidUtil.getCpuAbi(iDevice);
        String minitouchFilePath = String.format(MINITOUCH_PATH, cpuAbi);

        String androidDeviceMinitouchPath = AndroidDevice.TMP_FOLDER + "minitouch";
        log.info("[minitouch][{}]push minitouch到手机, {}->{}", deviceId, minitouchFilePath, androidDeviceMinitouchPath);
        iDevice.pushFile(minitouchFilePath, androidDeviceMinitouchPath);

        String chmodShellCmd = String.format(MINITOUCH_CHMOD_SHELL, androidDeviceMinitouchPath);
        log.info("[minitouch][{}]{} ", deviceId, chmodShellCmd);
        iDevice.executeShellCommand(chmodShellCmd, new NullOutputReceiver());
    }
}