package com.daxiang.actions.android;

import com.daxiang.actions.utils.AndroidUtil;
import com.daxiang.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class ClearAppData {

    private MacacaClient macacaClient;

    public ClearAppData(MacacaClient macacaClient) {
        this.macacaClient = macacaClient;
    }

    /**
     * 清除APP数据
     * @param packageName
     * @throws Exception
     */
    public void excute(String packageName) throws IOException {
        String deviceId = MacacaUtil.getDeviceId(macacaClient);
        AndroidUtil.clearAppData(deviceId,packageName);
    }
}
