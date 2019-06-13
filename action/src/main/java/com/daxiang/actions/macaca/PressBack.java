package com.daxiang.actions.macaca;

import macaca.client.MacacaClient;


/**
 * Created by jiangyitao.
 */
public class PressBack {

    private MacacaClient driver;

    public PressBack(MacacaClient driver) {
        this.driver = driver;
    }

    public void excute() throws Exception {
        driver.keys("4");
    }
}
