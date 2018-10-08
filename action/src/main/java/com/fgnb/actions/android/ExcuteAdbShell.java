package com.fgnb.actions.android;

import com.fgnb.actions.Action;
import com.fgnb.actions.utils.AndroidUtil;
import com.fgnb.actions.utils.MacacaUtil;
import com.fgnb.actions.utils.ShellExecutor;
import macaca.client.MacacaClient;


/**
 * Created by jiangyitao.
 */
public class ExcuteAdbShell extends Action {
    public ExcuteAdbShell(MacacaClient macacaClient) {
        super(macacaClient);
    }

    @Override
    public String excute(String... params) throws Exception {
        String deviceId = MacacaUtil.getDeviceId(macacaClient);
        String cmd = params[0];
        System.out.println("["+deviceId+"]执行adb shell命令:"+cmd);
        AndroidUtil.excuteCmd(deviceId,cmd);
        return null;
    }
}
