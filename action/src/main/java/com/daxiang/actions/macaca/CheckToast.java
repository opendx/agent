package com.daxiang.actions.macaca;

import macaca.client.MacacaClient;


/**
 * Created by jiangyitao.
 */
public class CheckToast {

    private MacacaClient driver;

    public CheckToast(MacacaClient driver) {
        this.driver = driver;
    }

    /**
     * 检查toast
     */
    public void excute(Object toast, Object checkTimeOfSecond) {
        long checkTimeOfMs = Integer.parseInt((String) checkTimeOfSecond) * 1000;
        String _toast = (String) toast;

        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime > checkTimeOfMs) {
                throw new RuntimeException("超时未检测到toast: " + toast);
            }
            try {
                if (_toast.equals(driver.getToast())) {
                    return;
                }
            } catch (Exception e) {
            }
        }
    }
}
