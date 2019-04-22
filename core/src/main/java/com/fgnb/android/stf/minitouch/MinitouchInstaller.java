package com.fgnb.android.stf.minitouch;

import com.android.ddmlib.*;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidUtils;
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

        String cpuAbi = AndroidUtils.getCpuAbi(iDevice);
        String minitouchFilePath = String.format(MINITOUCH_PATH, cpuAbi);

        String phoneMinitouchPath = AndroidDevice.TMP_FOLDER + "minitouch";
        log.info("[{}][minitouch]push minitouch到手机,{}->{}", deviceId, minitouchFilePath, phoneMinitouchPath);
        iDevice.pushFile(minitouchFilePath, phoneMinitouchPath);

        String chmodShellCmd = String.format(MINITOUCH_CHMOD_SHELL, phoneMinitouchPath);
        log.info("[{}][minitouch]{} ", deviceId, chmodShellCmd);
        iDevice.executeShellCommand(chmodShellCmd, new NullOutputReceiver());
    }
}
