package com.fgnb.actions.macaca;

import com.fgnb.actions.Action;
import macaca.client.MacacaClient;


/**
 * Created by jiangyitao.
 */
public class PressHome extends Action {
    public PressHome(MacacaClient macacaClient) {
        super(macacaClient);
    }

    @Override
    public String excute(String... params) throws Exception {
        macacaClient.keys("3");
        return null;
    }
}
