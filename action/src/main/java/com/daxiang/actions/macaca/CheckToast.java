package com.daxiang.actions.macaca;

import macaca.client.MacacaClient;


/**
 * Created by jiangyitao.
 */
public class CheckToast {

    private MacacaClient macacaClient;

    public CheckToast(MacacaClient macacaClient) {
        this.macacaClient = macacaClient;
    }

    /**
     * 检查toast
     * @param toast
     * @param timeoutSecond
     */
    public void excute(String toast,String timeoutSecond) {

        int timeout_second = (Integer.parseInt(timeoutSecond))*1000;

        long startTime = System.currentTimeMillis();
        while(true){
            if(System.currentTimeMillis() - startTime > timeout_second){
                throw new RuntimeException("超时未检测到toast:"+toast);
            }
            try {
                String acturalToast = macacaClient.getToast();
                if(toast.equals(acturalToast)){
                    //检测到toast一致
                    return;
                }
            } catch (Exception e) {
                //ignore
            }
        }
    }
}
