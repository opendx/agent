package com.fgnb.actions.web;

import org.openqa.selenium.WebDriver;

/**
 * Created by jiangyitao.
 */
public class OpenURL {

    private WebDriver driver;

    public OpenURL(WebDriver driver) {
        this.driver = driver;
    }

    public void excute(String url) {
        driver.get(url);
    }
}
