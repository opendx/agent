package com.fgnb.actions;

import org.openqa.selenium.WebDriver;

/**
 * Created by jiangyitao.
 */
public abstract class WebAction {
    protected WebDriver driver;

    public WebAction(WebDriver driver){
        this.driver = driver;
    }

    public abstract String excute(String... params) throws Exception;
}
