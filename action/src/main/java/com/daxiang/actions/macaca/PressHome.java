package com.daxiang.actions.macaca;

import macaca.client.MacacaClient;


/**
 * Created by jiangyitao.
 */
public class PressHome {

    private MacacaClient driver;

    public PressHome(MacacaClient driver) {
        this.driver = driver;
    }

    public void excute() throws Exception {
        driver.keys("3");
    }
}
