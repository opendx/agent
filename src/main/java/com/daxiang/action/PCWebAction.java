package com.daxiang.action;

import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Created by jiangyitao.
 * id: 4000 - 4999
 * platform = [3]
 */
public class PCWebAction extends BaseAction {

    private RemoteWebDriver driver;

    public PCWebAction(RemoteWebDriver driver) {
        super(driver);
        this.driver = driver;
    }
}
