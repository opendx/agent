package com.fgnb.actions.web;

import com.fgnb.actions.WebAction;
import org.openqa.selenium.WebDriver;

/**
 * Created by jiangyitao.
 */
public class OpenURL extends WebAction{
    public OpenURL(WebDriver driver) {
        super(driver);
    }

    @Override
    public String excute(String... params) throws Exception {
        driver.get(params[0]);
        return null;
    }
}
