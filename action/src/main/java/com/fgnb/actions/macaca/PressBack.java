package com.fgnb.actions.macaca;

import macaca.client.MacacaClient;


/**
 * Created by jiangyitao.
 */
public class PressBack {

    private MacacaClient macacaClient;

    public PressBack(MacacaClient macacaClient) {
        this.macacaClient = macacaClient;
    }

    public void excute() throws Exception {
        macacaClient.keys("4");
    }
}
