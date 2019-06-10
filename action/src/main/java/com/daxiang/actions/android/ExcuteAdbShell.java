package com.daxiang.actions.android;

import com.daxiang.actions.utils.AndroidUtil;
import com.daxiang.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;

import java.io.IOException;


/**
 * Created by jiangyitao.
 */
public class ExcuteAdbShell {

    private MacacaClient macacaClient;

    public ExcuteAdbShell(MacacaClient macacaClient) {
        this.macacaClient = macacaClient;
    }

    /**
     * 执行adb命令
     * @param cmd
     * @throws IOException
     */
    public void excute(String cmd) throws IOException {
        String deviceId = MacacaUtil.getDeviceId(macacaClient);
        AndroidUtil.excuteCmd(deviceId,cmd);
    }
}
