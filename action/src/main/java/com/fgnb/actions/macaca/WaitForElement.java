package com.fgnb.actions.macaca;

import com.fgnb.actions.Action;
import com.fgnb.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;

/**
 * Created by jiangyitao.
 */
public class WaitForElement extends Action {

    public WaitForElement(MacacaClient macacaClient) {
        super(macacaClient);
    }

    @Override
    public String excute(String... params) throws Exception {
        String findBy = params[0];
        String value = params[1];
        String timeout_second = params[2];
        int timeOut;
        try{
            timeOut = (Integer.parseInt(timeout_second))*1000;
        }catch (Exception e){
            timeOut = 1;
        }
        MacacaUtil.waitForElement(macacaClient,findBy,value,timeOut);
        return null;
    }

}

