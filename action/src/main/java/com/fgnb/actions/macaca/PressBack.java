package com.fgnb.actions.macaca;

import com.fgnb.actions.Action;
import macaca.client.MacacaClient;


/**
 * Created by jiangyitao.
 */
public class PressBack extends Action {

    public PressBack(MacacaClient macacaClient) {
        super(macacaClient);
    }

    @Override
    public String excute(String... params) throws Exception {
        macacaClient.keys("4");
        return null;
    }
}
