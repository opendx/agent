package com.fgnb.actions.macaca;

import com.fgnb.actions.Action;
import macaca.client.MacacaClient;

import java.util.Map;

/**
 * Created by jiangyitao.
 */
public class CheckToast extends Action {

    public CheckToast(MacacaClient macacaClient) {
        super(macacaClient);
    }

    @Override
    public String excute(String... params) throws Exception {

        String toast = params[0];
        int timeout_second = (Integer.parseInt(params[1]))*1000;

        long startTime = System.currentTimeMillis();
        while(true){
            if(System.currentTimeMillis() - startTime > timeout_second){
                throw new RuntimeException("超时未检测到toast:"+toast);
            }
            String acturalToast = macacaClient.getToast();
            if(toast.equals(acturalToast)){
                //检测到toast一致
                return null;
            }
        }
    }
}
