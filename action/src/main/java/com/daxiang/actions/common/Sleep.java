package com.daxiang.actions.common;

/**
 * Created by jiangyitao.
 */
public class Sleep {

    /**
     * 休眠
     */
    public void excute(Object second) throws InterruptedException {
        long sleepTime = Integer.parseInt((String) second) * 1000;
        Thread.sleep(sleepTime);
    }
}
