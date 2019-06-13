package com.daxiang.actions.android;

import com.daxiang.actions.utils.AndroidUtil;
import com.daxiang.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class ClearApkData {

    private MacacaClient driver;

    public ClearApkData(MacacaClient driver) {
        this.driver = driver;
    }

    /**
     * 清除apk数据
     */
    public void excute(Object packageName) throws IOException {
        String _packageName = (String) packageName;
        String deviceId = MacacaUtil.getDeviceId(driver);
        AndroidUtil.clearApkData(deviceId, _packageName);
    }
}
