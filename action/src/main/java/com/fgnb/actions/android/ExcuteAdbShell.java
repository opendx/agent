package com.fgnb.actions.android;

import com.fgnb.actions.utils.AndroidUtil;
import com.fgnb.actions.utils.MacacaUtil;
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
        System.out.println("["+deviceId+"]执行adb shell命令:"+cmd);
        AndroidUtil.excuteCmd(deviceId,cmd);
    }
}
