package com.daxiang.action.common;

import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class Sleep {

    public void execute(Object sleepTimeInSeconds) throws InterruptedException {
        Assert.notNull(sleepTimeInSeconds, "休眠时长不能为空");
        long sleepTime = (long)(Float.parseFloat((String) sleepTimeInSeconds) * 1000);
        Thread.sleep(sleepTime);
    }
}
