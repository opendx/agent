package com.daxiang.core.pc.web;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.util.StringUtils;

/**
 * Created by jiangyitao.
 */
public class FirefoxDevice extends BrowserDevice {

    public FirefoxDevice(Browser browser, BrowserServer browserServer) {
        super(browser, browserServer);
    }

    @Override
    protected Capabilities createCapabilities() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();

        if (!StringUtils.isEmpty(browser.getPath())) {
            firefoxOptions.setBinary(browser.getPath());
        }

        firefoxOptions.setAcceptInsecureCerts(true);

        return firefoxOptions;
    }
}
