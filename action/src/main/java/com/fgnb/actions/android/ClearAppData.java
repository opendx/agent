package com.fgnb.actions.android;

import com.fgnb.actions.Action;
import com.fgnb.actions.utils.AndroidUtil;
import com.fgnb.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;

/**
 * Created by jiangyitao.
 */
public class ClearAppData extends Action{

    public ClearAppData(MacacaClient macacaClient) {
        super(macacaClient);
    }

    @Override
    public String excute(String... params) throws Exception {
        String packageName = params[0];
        String deviceId = MacacaUtil.getDeviceId(macacaClient);

        AndroidUtil.clearAppData(deviceId,packageName);
        return null;
    }
}
