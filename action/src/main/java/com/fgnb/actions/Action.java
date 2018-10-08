package com.fgnb.actions;

import macaca.client.MacacaClient;

import java.util.Map;

/**
 * Created by jiangyitao.
 */
public abstract class Action {
    protected MacacaClient macacaClient;

    public Action(MacacaClient macacaClient){
        this.macacaClient = macacaClient;
    }

    public abstract String excute(String... params) throws Exception;
}
