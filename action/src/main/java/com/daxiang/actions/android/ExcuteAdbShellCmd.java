package com.daxiang.actions.android;

import com.daxiang.actions.utils.AndroidUtil;
import com.daxiang.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;

import java.io.IOException;


/**
 * Created by jiangyitao.
 */
public class ExcuteAdbShellCmd {

    private MacacaClient driver;

    public ExcuteAdbShellCmd(MacacaClient driver) {
        this.driver = driver;
    }

    /**
     * 执行adb命令
     */
    public void excute(Object cmd) throws IOException {
        String _cmd = (String) cmd;
        String deviceId = MacacaUtil.getDeviceId(driver);
        AndroidUtil.excuteAdbShellCmd(deviceId, _cmd);
    }
}
