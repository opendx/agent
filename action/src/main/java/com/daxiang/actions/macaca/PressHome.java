package com.daxiang.actions.macaca;

import macaca.client.MacacaClient;


/**
 * Created by jiangyitao.
 */
public class PressHome {

    private MacacaClient macacaClient;

    public PressHome(MacacaClient macacaClient) {
        this.macacaClient = macacaClient;
    }

    public void excute() throws Exception {
        macacaClient.keys("3");
    }
}
