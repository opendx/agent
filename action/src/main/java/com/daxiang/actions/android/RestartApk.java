package com.daxiang.actions.android;

import com.daxiang.actions.utils.AndroidUtil;
import com.daxiang.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class RestartApk {

    private MacacaClient driver;

    public RestartApk(MacacaClient driver) {
        this.driver = driver;
    }

    /**
     * 启动/重启apk
     */
    public void excute(Object packageName, Object launchActivity) throws IOException {
        String _packageName = (String) packageName;
        String _launchActivity = (String) launchActivity;
        String deviceId = MacacaUtil.getDeviceId(driver);
        AndroidUtil.restartApk(deviceId, _packageName, _launchActivity);
    }
}
