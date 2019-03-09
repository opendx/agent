package com.fgnb.actions.macaca;

import com.fgnb.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;

/**
 * Created by jiangyitao.
 */
public class WaitForElement {

    private MacacaClient macacaClient;

    public WaitForElement(MacacaClient macacaClient) {
        this.macacaClient = macacaClient;
    }

    public void excute(String findBy,String value,String timeoutSecond) throws Exception {
        int timeOut;
        try{
            timeOut = (Integer.parseInt(timeoutSecond))*1000;
        }catch (Exception e){
            timeOut = 1;
        }
        MacacaUtil.waitForElement(macacaClient,findBy,value,timeOut);
    }

}

