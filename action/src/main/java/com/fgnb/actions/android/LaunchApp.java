package com.fgnb.actions.android;

import com.fgnb.actions.Action;
import com.fgnb.actions.utils.AndroidUtil;
import com.fgnb.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;

/**
 * Created by jiangyitao.
 */
public class LaunchApp extends Action {

    public LaunchApp(MacacaClient macacaClient) {
        super(macacaClient);
    }

    @Override
    public String excute(String... params) throws Exception {
        String packageName = params[0];
        String launchActivity = params[1];

        String deviceId = MacacaUtil.getDeviceId(macacaClient);

        AndroidUtil.restartAppByAdbShell(deviceId,packageName,launchActivity);
        return null;
    }
}
