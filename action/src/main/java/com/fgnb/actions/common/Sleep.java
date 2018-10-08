package com.fgnb.actions.common;

import com.fgnb.actions.Action;
import macaca.client.MacacaClient;

import java.util.Map;

/**
 * Created by jiangyitao.
 */
public class Sleep extends Action {

    public Sleep(MacacaClient macacaClient) {
        super(macacaClient);
    }

    @Override
    public String excute(String... params) throws Exception {
        long ms = Long.parseLong(params[0]);
        Thread.sleep(ms);
        return null;
    }
}
