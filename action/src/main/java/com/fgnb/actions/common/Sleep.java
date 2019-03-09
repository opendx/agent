package com.fgnb.actions.common;

/**
 * Created by jiangyitao.
 */
public class Sleep {

    /**
     * 休眠
     * @param params
     */
    public void excute(String params) throws InterruptedException {
        long ms = Long.parseLong(params);
        Thread.sleep(ms);
    }
}
